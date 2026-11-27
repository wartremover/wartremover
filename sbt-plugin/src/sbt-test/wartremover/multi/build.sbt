lazy val commonSettings = Def.settings(
  scalaVersion := "2.13.18",
  wartremoverWarnings ++= Warts.all,
  wartremoverWarnings += Wart.custom("org.wartremover.contrib.warts.OldTime"),
  wartremoverCrossVersion := CrossVersion.binary,
  wartremoverDependencies += {
    ("org.wartremover" %% "wartremover-contrib" % "2.4.5").cross(wartremoverCrossVersion.value)
  },
)

lazy val myWarts = project
  .in(file("my-warts"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      ("org.wartremover" % "wartremover" % wartremover.Wart.PluginVersion).cross(CrossVersion.full)
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
  TaskKey[Unit]("checkClasspathOptions") := {
    val cpOptions = (Compile / scalacOptions).value.filter(_.startsWith("-P:wartremover:cp:"))
    assert(cpOptions.nonEmpty, "wartremover classpath options are empty")
    if (sbtVersion.value.startsWith("2")) {
      // machine specific paths in scalac options break the cache hit between machines
      val machinePaths = Seq(
        (LocalRootProject / baseDirectory).value.getCanonicalPath,
        csrCacheDirectory.value.getCanonicalPath,
      )
      cpOptions.foreach { opt =>
        assert(machinePaths.forall(p => !opt.contains(p)), s"machine specific path in ${opt}")
        assert(opt.contains("${"), s"virtual id is not used in ${opt}")
      }
      assert(
        extraIncOptions.value.exists(_._1 == "wartremover.classpath.sha256"),
        "wartremover.classpath.sha256 is missing from extraIncOptions"
      )
    }
  },
)
