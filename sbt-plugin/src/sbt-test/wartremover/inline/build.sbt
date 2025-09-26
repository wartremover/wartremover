lazy val commonSettings = Def.settings(
  scalaVersion := "3.7.3",
  ThisBuild / wartremoverCrossVersion := CrossVersion.binary,
  libraryDependencies += "org.playframework" %% "play-json" % "3.0.5",
)

lazy val myWarts = project
  .in(file("my-warts"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.wartremover" % "wartremover" % wartremover.Wart.PluginVersion cross (ThisBuild / wartremoverCrossVersion).value
    )
  )

lazy val main = project
  .in(file("main"))
  .settings(
    commonSettings,
    scalacOptions += "-P:wartremover:loglevel:debug",
    wartremoverErrors += Wart.custom("mywarts.InlineTest"),
    wartremover.WartRemover.dependsOnLocalProjectWarts(myWarts),
  )

commonSettings
