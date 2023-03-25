package wartremover

import java.nio.file.Path
import java.net.URL
import java.nio.charset.StandardCharsets
import org.wartremover.InspectParam
import org.wartremover.InspectResult
import sbt.*
import sbt.Keys.*
import sjsonnew.JsonFormat
import sjsonnew.support.scalajson.unsafe.CompactPrinter

object WartRemover extends sbt.AutoPlugin {
  override def trigger = allRequirements
  object autoImport {
    val WartremoverTag = Tags.Tag("wartremover")
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
      "3.2.2"
    },
    wartremoverInspectSettings := Nil,
    excludeLintKeys += wartremoverInspectOutputFile,
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
      (p / configuration / fullClasspath).value.map(_.data).map { f =>
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
    caseClass8(InspectParam, InspectParam.unapply)(
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
      caseClass8(org.wartremover.Position, org.wartremover.Position.unapply)(
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
      caseClass3(org.wartremover.Diagnostic, org.wartremover.Diagnostic.unapply)(
        "message",
        "wart",
        "position",
      )

    caseClass2(InspectResult.apply, InspectResult.unapply)(
      "errors",
      "warnings",
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
    x / wartremoverInspect := Def.taskDyn {
      val log = streams.value.log
      val myProject = thisProjectRef.value
      val thisTaskName = s"${myProject.project}/${x.id}/${wartremoverInspect.key.label}"
      def skipLog(reason: String) = {
        log.info(s"skip ${thisTaskName} because ${reason}")
        InspectResult.empty
      }
      if (scalaBinaryVersion.value == "3") {
        val errorWartNames = (x / wartremoverInspect / wartremoverErrors).value
        val warningWartNames = (x / wartremoverInspect / wartremoverWarnings).value
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
                val dependenciesClasspath = (x / dependencyClasspath).value

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
                  dependenciesClasspath = dependenciesClasspath.map(_.data.getAbsolutePath).toList,
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
                val Seq(launcher) = dependencyResolution.value
                  .retrieve(
                    dependencyId = "org.scala-sbt" % "sbt-launch" % (wartremoverInspect / sbtVersion).value,
                    scalaModuleInfo = scalaModuleInfo.value,
                    retrieveDirectory = csrCacheDirectory.value,
                    log = streams.value.log
                  )
                  .left
                  .map(e => throw e.resolveException)
                  .merge
                  .distinct
                val resultJson = runInspector(
                  projectName = thisTaskName,
                  base = (LocalRootProject / baseDirectory).value,
                  param = param,
                  scalaV = wartremoverInspectScalaVersion.value,
                  launcher = launcher,
                  (x / sources).value,
                  forkOptions = (wartremoverInspect / forkOptions).value,
                  extraSettings = wartremoverInspectSettings.value,
                ).fold(e => sys.error(s"${thisTaskName} failed ${e}"), identity)
                val result = {
                  val r = resultJson.decodeFromJsonString[InspectResult]
                  new InspectResult(errors = r.errors, warnings = r.warnings) {
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
    }.value
  )

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

  // Workaround for https://github.com/wartremover/wartremover/issues/123
  private[wartremover] def derive[T](s: Setting[T]): Setting[T] = {
    try {
      Def derive s
    } catch {
      case _: LinkageError =>
        import scala.language.reflectiveCalls
        Def
          .asInstanceOf[{
              def derive[T](
                setting: Setting[T],
                allowDynamic: Boolean,
                filter: Scope => Boolean,
                trigger: AttributeKey[_] => Boolean,
                default: Boolean
              ): Setting[T]
            }
          ]
          .derive(s, false, _ => true, _ => true, false)
    }
  }
}
