lazy val commonSettings = Def.settings(
  scalaVersion := "2.13.12",
  wartremoverWarnings ++= Warts.all,
  wartremoverWarnings += Wart.custom("org.wartremover.contrib.warts.OldTime"),
  wartremoverCrossVersion := CrossVersion.binary,
  wartremoverDependencies += {
    "org.wartremover" %% "wartremover-contrib" % "2.1.0" cross wartremoverCrossVersion.value
  },
)

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
  scalacOptions += "-P:wartremover:loglevel:debug",
  TaskKey[Unit]("writeUnimplementedSource") := {
    IO.write(
      (Compile / scalaSource).value / "C.scala",
      "object C { def c = ??? }"
    )
  },
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
