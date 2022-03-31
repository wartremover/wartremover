// https://github.com/sbt/sbt/issues/6860
val root = project.in(file("."))

Compile / wartremoverInspect / wartremoverWarnings ++= Warts.all

scalaVersion := "3.1.2"

wartremoverExcluded += (baseDirectory.value / "src/main/scala/ignore")

ThisBuild / wartremoverCrossVersion := CrossVersion.binary

Compile / wartremoverInspectOutputFile := Some(baseDirectory.value / "warts-main.json")

Test / wartremoverInspectOutputFile := Some(baseDirectory.value / "warts-test.json")
