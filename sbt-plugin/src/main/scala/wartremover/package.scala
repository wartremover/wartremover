import sbt._
import Keys._
import wartremover.WartRemover.autoImport.wartremoverCrossVersion

/** sbt-wartremover's key definitions */
package object wartremover {
  val wartremoverErrors = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation errors.")
  val wartremoverWarnings = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation warnings.")
  val wartremoverExcluded = taskKey[Seq[File]]("List of files to be excluded from all checks.")
  val wartremoverClasspaths = taskKey[Seq[String]]("List of classpaths for custom Warts")

  lazy val wartremoverSettings: Seq[sbt.Def.Setting[_]] = Seq(
    libraryDependencies += {
      compilerPlugin("org.wartremover" %% "wartremover" % Wart.PluginVersion cross wartremoverCrossVersion.value)
    }
  ) ++ inScope(Scope.ThisScope)(Seq(
    derive(scalacOptions ++= wartremoverErrors.value.distinct map (w => s"-P:wartremover:traverser:${w.clazz}")),
    derive(scalacOptions ++= wartremoverWarnings.value.distinct filterNot (wartremoverErrors.value contains _) map (w => s"-P:wartremover:only-warn-traverser:${w.clazz}")),
    derive(scalacOptions ++= wartremoverExcluded.value.distinct map (c => s"-P:wartremover:excluded:${c.getAbsolutePath}")),
    derive(scalacOptions ++= wartremoverClasspaths.value.distinct map (cp => s"-P:wartremover:cp:$cp"))
  ))

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
