package wartremover

import java.nio.file.Path
import java.net.URL
import org.wartremover.InspectParam
import org.wartremover.InspectResult
import sbt.LocalProject
import sbt.*
import sbt.Keys.*
import sbt.internal.librarymanagement.IvySbt
import sbt.librarymanagement.UnresolvedWarningConfiguration
import sbt.librarymanagement.UpdateConfiguration
import sbt.librarymanagement.ivy.IvyDependencyResolution
import sjsonnew.JsonFormat
import sjsonnew.support.scalajson.unsafe.CompactPrinter

object WartRemover extends sbt.AutoPlugin {
  override def trigger = allRequirements
  object autoImport {
    val wartremoverFailIfWartLoadError = settingKey[Boolean]("")
    val wartremoverInspect = taskKey[InspectResult]("run wartremover by TASTy inspector")
    val wartremoverInspectOutputFile = settingKey[Option[File]]("")
    val wartremoverInspectOutputStandardReporter = settingKey[Boolean]("")
    val wartremoverInspectFailOnErrors = settingKey[Boolean]("")
    val wartremoverInspectScalaVersion = settingKey[String]("scala version for wartremoverInspect task")
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

  private[this] def sequentialAndAggregate[A](
    tasks: List[Def.Initialize[Task[A]]],
    acc: List[A]
  ): Def.Initialize[Task[List[A]]] = {
    tasks match {
      case Nil =>
        Def.task {
          acc
        }
      case x :: xs =>
        Def.taskDyn {
          sequentialAndAggregate(xs, x.value :: acc)
        }
    }
  }

  override def globalSettings = Seq(
    wartremoverInspectScalaVersion := {
      // need NIGHTLY version because there are some bugs in old tasty-inspector.
      "3.1.3-RC1-bin-20220401-4a96ce7-NIGHTLY"
    },
    excludeLintKeys += wartremoverInspectOutputFile,
    wartremoverCrossVersion := CrossVersion.full,
    wartremoverDependencies := Nil,
    wartremoverErrors := Nil,
    wartremoverWarnings := Nil,
    wartremoverExcluded := Nil,
    wartremoverClasspaths := Nil
  )

  private[this] lazy val generateProject = {
    val id = "wartremover-inspector-project"
    Project(id = id, base = file("target") / id).settings(
      run / fork := true,
      fork := true,
      autoScalaLibrary := false,
      scalaVersion := wartremoverInspectScalaVersion.value,
      libraryDependencies := {
        if (scalaBinaryVersion.value == "3") {
          Seq(
            "org.scala-lang" % "scala3-tasty-inspector_3" % wartremoverInspectScalaVersion.value,
            "org.wartremover" % "wartremover-inspector_3" % Wart.PluginVersion,
          )
        } else {
          Nil
        }
      }
    )
  }

  // avoid extraProjects https://github.com/sbt/sbt/issues/4947
  override def derivedProjects(proj: ProjectDefinition[?]): Seq[Project] = {
    proj.projectOrigin match {
      case ProjectOrigin.DerivedProject =>
        Nil
      case _ =>
        Seq(generateProject)
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
          Def.taskIf {
            if ((x / tastyFiles).value.isEmpty) {
              skipLog(s"${tastyFiles.key.label} is empty")
            } else {
              import scala.language.reflectiveCalls
              val loader = (generateProject / Test / testLoader).value
              val clazz = loader.loadClass("org.wartremover.WartRemoverInspector")
              val instance =
                clazz.getConstructor().newInstance().asInstanceOf[{ def runFromJson(json: String): String }]

              val dependenciesClasspath = (x / fullClasspath).value

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
                      new URL(a)
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
              val resultJson = instance.runFromJson(param.toJsonString)
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
        Some((LocalRootProject / target).value / "compiler_plugins")
      } else {
        None
      }
    },
    inScope(Scope.ThisScope)(
      Seq(
        wartremoverClasspaths ++= {
          val ivy = ivySbt.value
          val s = streams.value
          wartremoverDependencies.value.map { m =>
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
            val a = getArtifact(moduleId, ivy, s)
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

  /**
   * [[https://github.com/lightbend/mima/blob/723bd0046c0c6a4f52c91ddc752d08dce3b7ba37/sbtplugin/src/main/scala/com/typesafe/tools/mima/plugin/SbtMima.scala#L79-L100]]
   * @note avoid coursier for sbt 1.2.x compatibility
   */
  private[this] def getArtifact(m: ModuleID, ivy: IvySbt, s: TaskStreams): File = {
    val depRes = IvyDependencyResolution(ivy.configuration)
    val module = depRes.wrapDependencyInModule(m)
    val uc = UpdateConfiguration().withLogging(UpdateLogging.DownloadOnly)
    val uwc = UnresolvedWarningConfiguration()
    val report = depRes.update(module, uc, uwc, s.log).left.map(_.resolveException).toTry.get
    val jars = (for {
      config <- report.configurations.iterator
      module <- config.modules
      (artifact, file) <- module.artifacts
      if artifact.name == m.name
      if artifact.classifier.isEmpty
    } yield file).toList.distinct
    jars match {
      case jar :: Nil =>
        jar
      case Nil =>
        sys.error(s"Could not resolve: $m $jars")
      case jar :: _ =>
        s.log.info(s"multiple jar found $jars")
        jar
    }
  }

}
