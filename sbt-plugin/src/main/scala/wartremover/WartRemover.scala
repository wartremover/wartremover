package wartremover

import java.nio.file.Path
import sbt._
import sbt.Keys._
import sbt.internal.librarymanagement.IvySbt
import sbt.librarymanagement.{ UnresolvedWarningConfiguration, UpdateConfiguration }
import sbt.librarymanagement.ivy.IvyDependencyResolution

object WartRemover extends sbt.AutoPlugin {
  override def trigger = allRequirements
  object autoImport {
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

  override def globalSettings = Seq(
    autoImport.wartremoverCrossVersion := CrossVersion.full,
    autoImport.wartremoverDependencies := Nil,
    autoImport.wartremoverErrors := Nil,
    autoImport.wartremoverWarnings := Nil,
    autoImport.wartremoverExcluded := Nil,
    autoImport.wartremoverClasspaths := Nil
  )

  private[this] def copyToCompilerPluginJarsDir(src: File, jarDir: Option[File], base: File, log: Logger): Option[Path] = {
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
            jarDir = autoImport.wartremoverPluginJarsDir.value,
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
    autoImport.wartremoverClasspaths ++= {
      (fullClasspath in (p, configuration)).value.map(_.data).map{ f =>
        copyToCompilerPluginJarsDir(
          src = f,
          jarDir = autoImport.wartremoverPluginJarsDir.value,
          base = (LocalRootProject / baseDirectory).value,
          log = streams.value.log
        ).map("file:" + _).getOrElse(f.toURI.toString)
      }
    }
  }

  override lazy val projectSettings: Seq[Def.Setting[_]] = Def.settings(
    libraryDependencies += {
      compilerPlugin("org.wartremover" %% "wartremover" % Wart.PluginVersion cross autoImport.wartremoverCrossVersion.value)
    },
    scalacOptionSetting(scalacOptions),
    scalacOptionSetting(Compile / scalacOptions),
    scalacOptionSetting(Test / scalacOptions),
    scalacOptions ++= {
      // use relative path
      // https://github.com/sbt/sbt/issues/6027
      autoImport.wartremoverExcluded.value.distinct.map { c =>
        val base = (LocalRootProject / baseDirectory).value
        val x = base.toPath.relativize(c.toPath)
        s"-P:wartremover:excluded:$x"
      }
    },
    autoImport.wartremoverPluginJarsDir := {
      if (VersionNumber(sbtVersion.value).matchesSemVer(SemanticSelector(">=1.4.0"))) {
        Some((LocalRootProject / target).value / "compiler_plugins")
      } else {
        None
      }
    },
    inScope(Scope.ThisScope)(Seq(
      autoImport.wartremoverClasspaths ++= {
        val ivy = ivySbt.value
        val s = streams.value
        autoImport.wartremoverDependencies.value.map { m =>
          val moduleId = CrossVersion(cross = m.crossVersion, fullVersion = scalaVersion.value, binaryVersion = scalaBinaryVersion.value) match {
            case Some(f) =>
              m.withName(f(Project.normalizeModuleID(m.name)))
            case None =>
              m
          }
          val a = getArtifact(moduleId, ivy, s)
          copyToCompilerPluginJarsDir(
            src = a,
            jarDir = autoImport.wartremoverPluginJarsDir.value,
            base = (LocalRootProject / baseDirectory).value,
            log = streams.value.log
          ).map("file:" + _).getOrElse(a.toURI.toString)
        }
      },
      derive(scalacOptions ++= autoImport.wartremoverErrors.value.distinct map (w => s"-P:wartremover:traverser:${w.clazz}")),
      derive(scalacOptions ++= autoImport.wartremoverWarnings.value.distinct filterNot (autoImport.wartremoverErrors.value contains _) map (w => s"-P:wartremover:only-warn-traverser:${w.clazz}")),
      derive(scalacOptions ++= autoImport.wartremoverClasspaths.value.distinct map (cp => s"-P:wartremover:cp:$cp"))
    ))
  )

  // Workaround for https://github.com/wartremover/wartremover/issues/123
  private[wartremover] def derive[T](s: Setting[T]): Setting[T] = {
    try {
      Def derive s
    } catch {
      case _: LinkageError =>
        import scala.language.reflectiveCalls
        Def.asInstanceOf[{def derive[T](setting: Setting[T], allowDynamic: Boolean, filter: Scope => Boolean, trigger: AttributeKey[_] => Boolean, default: Boolean): Setting[T]}]
          .derive(s, false, _ => true, _ => true, false)
    }
  }

  /**
   * [[https://github.com/lightbend/mima/blob/723bd0046c0c6a4f52c91ddc752d08dce3b7ba37/sbtplugin/src/main/scala/com/typesafe/tools/mima/plugin/SbtMima.scala#L79-L100]]
   * @note avoid coursier for sbt 1.2.x compatibility
   */
  private[this] def getArtifact(
    m: ModuleID,
    ivy: IvySbt,
    s: TaskStreams): File = {
    val depRes = IvyDependencyResolution(ivy.configuration)
    val module = depRes.wrapDependencyInModule(m)
    val uc = UpdateConfiguration().withLogging(UpdateLogging.DownloadOnly)
    val uwc = UnresolvedWarningConfiguration()
    val report = depRes
      .update(module, uc, uwc, s.log)
      .left
      .map(_.resolveException)
      .toTry
      .get
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
