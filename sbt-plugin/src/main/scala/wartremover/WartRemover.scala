package wartremover

import java.nio.file.Path
import java.nio.charset.StandardCharsets
import org.wartremover.InspectParam
import org.wartremover.InspectResult
import sbt.Keys.*
import sjsonnew.JsonFormat
import sjsonnew.support.scalajson.unsafe.CompactPrinter
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Using
// format: off
import sbt.{given, *}
// format: on

object WartRemover extends sbt.AutoPlugin with WartRemoverCompat {
  override def trigger = allRequirements
  object autoImport {
    val WartremoverTag = Tags.Tag("wartremover")
    val wartremoverTask = InputKey[InspectResult]("wartremover", "run wartremover by TASTy inspector")
    val wartremoverFailIfWartLoadError = settingKey[Boolean]("")
    val wartremoverInspect = taskKey[InspectResult]("run wartremover by TASTy inspector")
    val wartremoverInspectOutputFile = settingKey[Option[File]]("")
    val wartremoverInspectOutputStandardReporter = settingKey[Boolean]("")
    val wartremoverInspectFailOnErrors = settingKey[Boolean]("")
    val wartremoverInspectScalaVersion = settingKey[String]("scala version for wartremoverInspect task")
    val wartremoverInspectSettings = settingKey[Seq[String]]("extra settings for run inspector")
    val wartremoverErrors = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation errors.")
    val wartremoverWarnings = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation warnings.")
    val wartremoverExcluded = taskKey[Seq[File]]("List of files to be excluded from all checks.")
    val wartremoverClasspaths = taskKey[Seq[String]]("List of classpaths for custom Warts")
    val wartremoverCrossVersion = settingKey[CrossVersion]("CrossVersion setting for wartremover")
    val wartremoverDependencies = settingKey[Seq[ModuleID]]("List of dependencies for custom Warts")
    val wartremoverPluginJarsDir = settingKey[Option[File]]("workaround for https://github.com/sbt/sbt/issues/6027")
    val Wart = wartremover.Wart
    val Warts = wartremover.Warts
  }
  import autoImport._

  override def globalSettings = Seq(
    Global / concurrentRestrictions += Tags.limit(WartremoverTag, 2),
    wartremoverInspectScalaVersion := {
      "3.3.4"
    },
    wartremoverInspectSettings := Nil,
    excludeLintKeys += wartremoverInspectOutputFile,
    excludeLintKeys += wartremoverInspectScalaVersion,
    wartremoverCrossVersion := CrossVersion.full,
    wartremoverDependencies := Nil,
    wartremoverErrors := Nil,
    wartremoverWarnings := Nil,
    wartremoverExcluded := Nil,
    wartremoverClasspaths := Nil
  )

  private[this] def runInspector(
    projectName: String,
    base: File,
    param: InspectParam,
    scalaV: String,
    launcher: File,
    scalaSources: Seq[File],
    forkOptions: ForkOptions,
    jarFiles: Seq[Seq[Byte]],
    extraSettings: Seq[String],
  ): Either[Int, String] = {
    val buildSbt =
      s"""autoScalaLibrary := false
         |name := "${projectName}"
         |logLevel := Level.Warn
         |scalaVersion := "${scalaV}"
         |libraryDependencies := Seq(
         |  "org.scala-lang" % "scala3-tasty-inspector_3" % "${scalaV}",
         |  "org.wartremover" % "wartremover-inspector_3" % "${Wart.PluginVersion}",
         |)
         |Compile / sources := Nil
         |${extraSettings.mkString("\n\n")}
         |""".stripMargin

    IO.withTemporaryDirectory { dir =>
      jarFiles.zipWithIndex.foreach { case (jarBytes, i) =>
        IO.write(dir / "lib" / s"warts${i}.jar", jarBytes.toArray)
      }
      val forkOpt = forkOptions.withWorkingDirectory(dir)
      val out = dir / "out.json"
      val in = dir / "in.json"
      IO.copy(
        scalaSources.flatMap { f =>
          IO.relativize(base, f).map { x =>
            f -> (dir / x)
          }
        }
      )
      IO.write(dir / "build.sbt", buildSbt.getBytes(StandardCharsets.UTF_8))
      IO.write(in, param.toJsonString.getBytes(StandardCharsets.UTF_8))
      val ret = Fork.java.apply(
        forkOpt,
        Seq(
          "-jar",
          launcher.getCanonicalPath,
          Seq(
            "runMain",
            "org.wartremover.WartRemoverInspector",
            s"--input=${in.getCanonicalPath}",
            s"--output=${out.getCanonicalPath}",
          ).mkString(" ")
        )
      )
      if (ret == 0) {
        Right(IO.read(out))
      } else {
        Left(ret)
      }
    }
  }

  private[this] def copyToCompilerPluginJarsDir(
    src: File,
    jarDir: Option[File],
    base: File,
    log: Logger
  ): Option[Path] = {
    jarDir match {
      case Some(compilerPluginsDir) =>
        if (src.isFile) {
          val jarName = src.getName
          val targetJar = compilerPluginsDir / jarName
          if (!targetJar.isFile) {
            IO.copyFile(
              sourceFile = src,
              targetFile = targetJar
            )
            log.debug(s"copy from $src to $targetJar")
          } else {
            log.debug(s"file $targetJar already exists")
          }
          Some(base.toPath.relativize(targetJar.toPath))
        } else {
          if (!src.isDirectory) {
            log.debug(s"neither file nor directory!? $src")
          }
          Some(base.toPath.relativize(src.toPath))
        }
      case None =>
        log.debug("jarDir is None")
        None
    }
  }

  def scalacOptionSetting(k: TaskKey[Seq[String]]): Def.SettingsDefinition = {
    k := {
      val prefix = "-Xplugin:"
      k.value.map { opt =>
        if (opt startsWith prefix) {
          val originalPluginFile = file(opt.drop(prefix.length))
          copyToCompilerPluginJarsDir(
            src = originalPluginFile,
            jarDir = wartremoverPluginJarsDir.value,
            base = (LocalRootProject / baseDirectory).value,
            log = streams.value.log
          ).map {
            prefix + _
          }.getOrElse(opt)
        } else {
          opt
        }
      }.distinct
    }
  }

  def dependsOnLocalProjectWarts(p: Reference, configuration: Configuration = Compile): Def.SettingsDefinition = {
    wartremoverClasspaths ++= {
      val converter = fileConverter.value
      (p / configuration / fullClasspath).value.map(_.data).map { virtualFile =>
        val f = convertToFile(virtualFile, converter)
        copyToCompilerPluginJarsDir(
          src = f,
          jarDir = wartremoverPluginJarsDir.value,
          base = (LocalRootProject / baseDirectory).value,
          log = streams.value.log
        ).map("file:" + _).getOrElse(f.toURI.toString)
      }
    }
  }

  private[this] implicit val inspectParamFormat: JsonFormat[InspectParam] = {
    import sjsonnew.BasicJsonProtocol.*
    caseClass8(InspectParam.apply, (_: InspectParam).asTupleOption)(
      "tastyFiles",
      "dependenciesClasspath",
      "wartClasspath",
      "errorWarts",
      "warningWarts",
      "exclude",
      "failIfWartLoadError",
      "outputStandardReporter",
    )
  }

  private[this] implicit val inspectResultFormat: JsonFormat[InspectResult] = {
    import sjsonnew.BasicJsonProtocol.*

    implicit val positionInstance: JsonFormat[org.wartremover.Position] =
      caseClass8(org.wartremover.Position.apply, (_: org.wartremover.Position).asTupleOption)(
        "start",
        "startLine",
        "startColumn",
        "end",
        "endLine",
        "endColumn",
        "path",
        "sourceCode",
      )

    implicit val diagnosticInstance: JsonFormat[org.wartremover.Diagnostic] =
      caseClass3(org.wartremover.Diagnostic.apply, (_: org.wartremover.Diagnostic).asTupleOption)(
        "message",
        "wart",
        "position",
      )

    caseClass3(InspectResult.apply, (_: InspectResult).asTupleOption)(
      "errors",
      "warnings",
      "stderr",
    )
  }

  private[this] implicit class JsonOps[A](private val self: A) extends AnyVal {
    def toJsonString(implicit w: sjsonnew.JsonWriter[A]): String = {
      val builder = new sjsonnew.Builder(sjsonnew.support.scalajson.unsafe.Converter.facade)
      w.write(self, builder)
      CompactPrinter.apply(
        builder.result.getOrElse(sys.error("invalid json"))
      )
    }
  }

  private[this] implicit class JsonStringOps(private val string: String) extends AnyVal {
    def decodeFromJsonString[A](implicit r: sjsonnew.JsonReader[A]): A = {
      val json = sjsonnew.support.scalajson.unsafe.Parser.parseUnsafe(string)
      val unbuilder = new sjsonnew.Unbuilder(sjsonnew.support.scalajson.unsafe.Converter.facade)
      r.read(Some(json), unbuilder)
    }
  }

  private[this] def inspectTask(x: Configuration): Seq[Def.Setting[?]] = Def.settings(
    x / wartremoverInspectOutputFile := None,
    wartremoverTaskSetting(x),
    x / wartremoverInspect := Def.taskDyn {
      createInspectTask(
        x = x,
        warningWartNames = (x / wartremoverInspect / wartremoverWarnings).value,
        errorWartNames = (x / wartremoverInspect / wartremoverErrors).value,
        jarFiles = Nil,
      )
    }.value
  )

  private[wartremover] final case class CompileResult(jarBinary: Option[Seq[Byte]], wartNames: Seq[Wart])
  private[wartremover] object CompileResult {
    val empty: CompileResult = CompileResult(None, Nil)
  }

  private[wartremover] def getAllClassNamesInJar(jar: File): List[String] = {
    val suffix = ".class"
    Using.resource(new ZipInputStream(new FileInputStream(jar))) { zip =>
      Iterator
        .continually(
          zip.getNextEntry
        )
        .takeWhile(_ != null)
        .filter(e => !e.isDirectory && e.getName.endsWith(suffix))
        .map(
          _.getName.replace('/', '.').dropRight(suffix.length)
        )
        .toList
    }
  }

  private[this] case class WartCacheKey(scalaV: String, files: Set[String])

  private[this] val wartRunCache: TrieMap[WartCacheKey, Future[Seq[Byte]]] = TrieMap.empty

  private[wartremover] def getJar(files: Set[String]): Def.Initialize[Task[Seq[Byte]]] = Def.task {
    val key = WartCacheKey(
      scalaV = (wartremoverTask / scalaVersion).value,
      files = files
    )
    val forkOps = (wartremoverTask / forkOptions).value
    val launcher = sbtLauncher(wartremoverTask).value
    val log = state.value.log

    val res = wartRunCache.getOrElseUpdate(
      key,
      Future {
        val buildSbt =
          s"""
             |logLevel := Level.Warn
             |scalaVersion := "${key.scalaV}"
             |crossPaths := false
             |libraryDependencies := Seq(
             |  "org.wartremover" %% "wartremover" % "${Wart.PluginVersion}"
             |)
             |""".stripMargin

        IO.withTemporaryDirectory { dir =>
          IO.write(dir / "build.sbt", buildSbt.getBytes(StandardCharsets.UTF_8))
          key.files.toSeq.sorted.zipWithIndex.foreach { case (src, index) =>
            IO.write(dir / s"${index}.scala", src.getBytes(StandardCharsets.UTF_8))
          }
          log.info(s"compile ${key.files.size} files")
          val exitCode = Fork.java.apply(
            forkOps.withWorkingDirectory(dir),
            Seq(
              "-jar",
              launcher.getCanonicalPath,
              packageBin.key.label
            )
          )
          assert(exitCode == 0, s"exit code = $exitCode")
          log.info(s"compiled ${key.files.size} files")
          val Seq(compiledJar) = (dir / "target").listFiles(f => f.isFile && f.getName.endsWith(".jar")).toList
          IO.readBytes(compiledJar).toSeq
        }
      }(ExecutionContext.global)
    )

    Await.result(res, 2.minutes)
  }

  private[wartremover] def createInspectTask(
    x: Configuration,
    errorWartNames: Seq[Wart],
    warningWartNames: Seq[Wart],
    jarFiles: Seq[Seq[Byte]],
  ): Def.Initialize[Task[InspectResult]] = Def.taskDyn {
    val log = streams.value.log
    val myProject = thisProjectRef.value
    val thisTaskName = s"${myProject.project}/${x.id}/${wartremoverInspect.key.label}"
    def skipLog(reason: String) = {
      log.info(s"skip ${thisTaskName} because ${reason}")
      InspectResult.empty
    }
    if (scalaBinaryVersion.value == "3") {
      if (errorWartNames.isEmpty && warningWartNames.isEmpty) {
        Def.task(skipLog("warts is empty"))
      } else {
        // avoid taskIf
        // https://github.com/sbt/sbt/issues/6862
        Def.taskDyn {
          if ((x / tastyFiles).value.isEmpty) {
            Def.task(skipLog(s"${tastyFiles.key.label} is empty"))
          } else {
            Def.task {
              val converter = fileConverter.value
              val dependenciesClasspath = (x / dependencyClasspath).value.map(x => convertToFile(x.data, converter))

              val logStr = {
                def names(xs: Seq[Wart]): List[String] = {
                  xs.map(_.clazz)
                    .distinct
                    .groupBy(a => a.split('.').lastOption.getOrElse(a))
                    .flatMap {
                      case (k, v) if v.size == 1 => k :: Nil
                      case (_, v) => v
                    }
                    .toList
                    .sorted
                }

                List(
                  if (errorWartNames.nonEmpty) {
                    names(errorWartNames).mkString("errorWarts = [", ", ", "].")
                  } else {
                    ""
                  },
                  if (warningWartNames.nonEmpty) {
                    names(warningWartNames).mkString("warningWarts = [", ", ", "]")
                  } else {
                    ""
                  }
                ).mkString(" ")
              }
              log.info(s"running ${thisTaskName}. ${logStr}")
              val param = org.wartremover.InspectParam(
                tastyFiles = (x / tastyFiles).value.map(_.getAbsolutePath).toList,
                dependenciesClasspath = dependenciesClasspath.map(_.getAbsolutePath).toList,
                wartClasspath = {
                  val filePrefix = "file:"
                  (x / wartremoverClasspaths).value.map {
                    case a if a.startsWith(filePrefix) =>
                      file(a.drop(filePrefix.length)).getCanonicalFile.toURI.toURL
                    case a =>
                      new URI(a).toURL
                  }.map(_.toString)
                }.toList,
                errorWarts = errorWartNames.map(_.clazz).toList,
                warningWarts = warningWartNames.map(_.clazz).toList,
                exclude = wartremoverExcluded.value.distinct.flatMap { c =>
                  val base = (LocalRootProject / baseDirectory).value
                  IO.relativize(base, c)
                }.toList,
                failIfWartLoadError = (x / wartremoverFailIfWartLoadError).value,
                outputStandardReporter = (x / wartremoverInspectOutputStandardReporter).value
              )
              val launcher = sbtLauncher(wartremoverInspect).value
              val resultJson = runInspector(
                projectName = thisTaskName,
                base = (LocalRootProject / baseDirectory).value,
                param = param,
                scalaV = wartremoverInspectScalaVersion.value,
                launcher = launcher,
                (x / sources).value,
                forkOptions = (wartremoverInspect / forkOptions).value,
                jarFiles = jarFiles,
                extraSettings = wartremoverInspectSettings.value,
              ).fold(e => sys.error(s"${thisTaskName} failed ${e}"), identity)
              val result = {
                val r = resultJson.decodeFromJsonString[InspectResult]
                new InspectResult(errors = r.errors, warnings = r.warnings, stderr = r.stderr) {
                  override def toString: String = resultJson
                }
              }
              (x / wartremoverInspectOutputFile).?.value.flatten.foreach { outFile =>
                log.info(s"[${thisProjectRef.value.project}] write result to ${outFile}")
                IO.write(outFile, resultJson)
              }
              if (result.errors.nonEmpty && (x / wartremoverInspectFailOnErrors).value) {
                sys.error(s"[${thisProjectRef.value.project}] wart error found")
              } else {
                log.info(s"finished ${thisTaskName}. found ${result.warnings.size} warnings")
                result
              }
            }.tag(WartremoverTag)
          }
        }
      }
    } else {
      Def.task(
        skipLog(s"scalaVersion is ${scalaVersion.value}. not Scala 3")
      )
    }
  }

  private[wartremover] def getJarFiles(module: ModuleID): Def.Initialize[Task[Seq[File]]] = Def.task {
    dependencyResolution.value
      .retrieve(
        dependencyId = module,
        scalaModuleInfo = scalaModuleInfo.value,
        retrieveDirectory = csrCacheDirectory.value,
        log = streams.value.log
      )
      .left
      .map(e => throw e.resolveException)
      .merge
      .distinct
  }

  private[this] def sbtLauncher[A](k: InputKey[A]): Def.Initialize[Task[File]] = Def.taskDyn {
    val v = (k / sbtVersion).value
    Def.task {
      val Seq(launcher) = getJarFiles("org.scala-sbt" % "sbt-launch" % v).value
      launcher
    }
  }

  private[this] def sbtLauncher[A](k: TaskKey[A]): Def.Initialize[Task[File]] = Def.taskDyn {
    val v = (k / sbtVersion).value
    Def.task {
      val Seq(launcher) = getJarFiles("org.scala-sbt" % "sbt-launch" % v).value
      launcher
    }
  }

  override lazy val projectSettings: Seq[Def.Setting[_]] = Def.settings(
    libraryDependencies ++= {
      Seq(
        compilerPlugin(
          "org.wartremover" %% "wartremover" % Wart.PluginVersion cross wartremoverCrossVersion.value
        )
      )
    },
    wartremoverFailIfWartLoadError := false,
    wartremoverInspectFailOnErrors := true,
    wartremoverInspectOutputStandardReporter := true,
    Seq(Compile, Test).flatMap(inspectTask),
    scalacOptionSetting(scalacOptions),
    scalacOptionSetting(Compile / scalacOptions),
    scalacOptionSetting(Test / scalacOptions),
    scalacOptions ++= {
      // use relative path
      // https://github.com/sbt/sbt/issues/6027
      wartremoverExcluded.value.distinct.map { c =>
        val base = (LocalRootProject / baseDirectory).value
        val x = base.toPath.relativize(c.toPath)
        s"-P:wartremover:excluded:$x"
      }
    },
    wartremoverPluginJarsDir := {
      if (VersionNumber(sbtVersion.value).matchesSemVer(SemanticSelector(">=1.4.0"))) {
        Some((LocalRootProject / crossTarget).value / "compiler_plugins")
      } else {
        None
      }
    },
    inScope(Scope.ThisScope)(
      Seq(
        wartremoverClasspaths ++= {
          val d = dependencyResolution.value
          val jars = wartremoverDependencies.value.flatMap { m =>
            val moduleId = CrossVersion(
              cross = m.crossVersion,
              fullVersion = scalaVersion.value,
              binaryVersion = scalaBinaryVersion.value
            ) match {
              case Some(f) =>
                m.withName(f(Project.normalizeModuleID(m.name)))
              case None =>
                m
            }
            d.retrieve(
              dependencyId = moduleId,
              scalaModuleInfo = scalaModuleInfo.value,
              retrieveDirectory = csrCacheDirectory.value,
              log = streams.value.log
            ).left
              .map(e => throw e.resolveException)
              .merge
          }.distinct
          jars.map { a =>
            copyToCompilerPluginJarsDir(
              src = a,
              jarDir = wartremoverPluginJarsDir.value,
              base = (LocalRootProject / baseDirectory).value,
              log = streams.value.log
            ).map("file:" + _).getOrElse(a.toURI.toString)
          }
        },
        derive(
          scalacOptions ++= {
            if (wartremoverFailIfWartLoadError.value) {
              Seq(s"-P:wartremover:on-wart-load-error:failure")
            } else {
              Nil
            }
          }
        ),
        derive(
          scalacOptions ++= {
            wartremoverErrors.value.distinct map (w => s"-P:wartremover:traverser:${w.clazz}")
          }
        ),
        derive(
          scalacOptions ++= {
            wartremoverWarnings.value.distinct filterNot (wartremoverErrors.value contains _) map (w =>
              s"-P:wartremover:only-warn-traverser:${w.clazz}"
            )
          }
        ),
        derive(
          scalacOptions ++= {
            wartremoverClasspaths.value.distinct map (cp => s"-P:wartremover:cp:$cp")
          }
        )
      )
    )
  )

  private[wartremover] def derive[T](s: Setting[T]): Setting[T] =
    Def derive s
}
