package wartremover

import sbt._
import sbt.Keys._

object WartRemover extends sbt.AutoPlugin {
  override def trigger = allRequirements
  object autoImport {
    val wartremoverErrors = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation errors.")
    val wartremoverWarnings = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation warnings.")
    val wartremoverExcluded = taskKey[Seq[File]]("List of files to be excluded from all checks.")
    val wartremoverClasspaths = taskKey[Seq[String]]("List of classpaths for custom Warts")
    val wartremoverCrossVersion = settingKey[CrossVersion]("CrossVersion setting for wartremover")
    val Wart = wartremover.Wart
    val Warts = wartremover.Warts
  }

  override def globalSettings = Seq(
    autoImport.wartremoverCrossVersion := CrossVersion.full,
    autoImport.wartremoverErrors := Nil,
    autoImport.wartremoverWarnings := Nil,
    autoImport.wartremoverExcluded := Nil,
    autoImport.wartremoverClasspaths := Nil
  )

  override lazy val projectSettings: Seq[Def.Setting[_]] = Def.settings(
    libraryDependencies += {
      compilerPlugin("org.wartremover" %% "wartremover" % Wart.PluginVersion cross autoImport.wartremoverCrossVersion.value)
    },
    inScope(Scope.ThisScope)(Seq(
      derive(scalacOptions ++= autoImport.wartremoverErrors.value.distinct map (w => s"-P:wartremover:traverser:${w.clazz}")),
      derive(scalacOptions ++= autoImport.wartremoverWarnings.value.distinct filterNot (autoImport.wartremoverErrors.value contains _) map (w => s"-P:wartremover:only-warn-traverser:${w.clazz}")),
      derive(scalacOptions ++= autoImport.wartremoverExcluded.value.distinct map (c => s"-P:wartremover:excluded:${c.getAbsolutePath}")),
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
}
