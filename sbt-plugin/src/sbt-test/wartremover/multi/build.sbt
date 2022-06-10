lazy val commonSettings = Def.settings(
  scalaVersion := "2.13.8",
  wartremoverWarnings ++= Warts.all,
  wartremoverWarnings += Wart.custom("org.wartremover.contrib.warts.OldTime"),
  wartremoverCrossVersion := CrossVersion.binary,
  wartremoverDependencies += {
    "org.wartremover" %% "wartremover-contrib" % "1.3.11" cross wartremoverCrossVersion.value
  },
)

scalaVersion := "2.12.16"

lazy val myWarts = project
  .in(file("my-warts"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.wartremover" % "wartremover" % wartremover.Wart.PluginVersion cross CrossVersion.full
    )
  )

lazy val a1 = project.settings(
  commonSettings,
  wartremoverErrors += Wart.custom("mywarts.Unimplemented"),
  wartremoverExcluded ++= (Compile / managedSourceDirectories).value,
  (Compile / sourceGenerators) += task {
    val dir = (Compile / sourceManaged).value
    val b = dir / "B.scala"
    IO.write(
      b,
      "object B { def b = ??? }"
    )
    Seq(b)
  },
  wartremover.WartRemover.dependsOnLocalProjectWarts(myWarts),
)
