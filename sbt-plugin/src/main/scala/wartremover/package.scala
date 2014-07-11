import sbt._
import Keys._

/** sbt-wartremover's key definitions */
package object wartremover {
  val wartremoverErrors = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation errors.")
  val wartremoverWarnings = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation warnings.")
  val wartremoverExcluded = settingKey[Seq[String]]("List of fully-qualified class and package names to be excluded from all checks.")
  val wartremoverClasspaths = settingKey[Seq[String]]("List of classpaths for custom Warts")

  lazy val wartremoverSettings: Seq[sbt.Def.Setting[_]] = Seq(
    wartremoverErrors := Seq.empty,
    wartremoverWarnings := Seq.empty,
    wartremoverExcluded := Seq.empty,
    wartremoverClasspaths := Seq.empty,

    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.brianmckenna" %% "wartremover" % Wart.PluginVersion)
  ) ++ inScope(Global)(Seq(
    Def.derive(scalacOptions ++= wartremoverErrors.value.distinct map (w => s"-P:wartremover:traverser:${w.clazz}")),
    Def.derive(scalacOptions ++= wartremoverWarnings.value.distinct filterNot (wartremoverErrors.value contains _) map (w => s"-P:wartremover:only-warn-traverser:${w.clazz}")),
    Def.derive(scalacOptions ++= wartremoverExcluded.value.distinct map (c => s"-P:wartremover:excluded:$c")),
    Def.derive(scalacOptions ++= wartremoverClasspaths.value.distinct map (cp => s"-P:wartremover:cp:$cp"))
  ))
}
