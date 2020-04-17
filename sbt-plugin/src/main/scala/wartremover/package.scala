import sbt._
import Keys._
import wartremover.WartRemover.autoImport

package object wartremover {
  @deprecated(message = "will be removed. use wartremover.WartRemover.autoImport.wartremoverErrors instead", since = "2.4.7")
  val wartremoverErrors = autoImport.wartremoverErrors
  @deprecated(message = "will be removed. use wartremover.WartRemover.autoImport.wartremoverWarnings instead", since = "2.4.7")
  val wartremoverWarnings = autoImport.wartremoverErrors
  @deprecated(message = "will be removed. use wartremover.WartRemover.autoImport.wartremoverExcluded instead", since = "2.4.7")
  val wartremoverExcluded = autoImport.wartremoverErrors
  @deprecated(message = "will be removed. use wartremover.WartRemover.autoImport.wartremoverClasspaths instead", since = "2.4.7")
  val wartremoverClasspaths = autoImport.wartremoverErrors

  lazy val wartremoverSettings: Seq[sbt.Def.Setting[_]] = Seq(
    libraryDependencies += {
      compilerPlugin("org.wartremover" %% "wartremover" % Wart.PluginVersion cross autoImport.wartremoverCrossVersion.value)
    }
  ) ++ inScope(Scope.ThisScope)(Seq(
    derive(scalacOptions ++= autoImport.wartremoverErrors.value.distinct map (w => s"-P:wartremover:traverser:${w.clazz}")),
    derive(scalacOptions ++= autoImport.wartremoverWarnings.value.distinct filterNot (autoImport.wartremoverErrors.value contains _) map (w => s"-P:wartremover:only-warn-traverser:${w.clazz}")),
    derive(scalacOptions ++= autoImport.wartremoverExcluded.value.distinct map (c => s"-P:wartremover:excluded:${c.getAbsolutePath}")),
    derive(scalacOptions ++= autoImport.wartremoverClasspaths.value.distinct map (cp => s"-P:wartremover:cp:$cp"))
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
