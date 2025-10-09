Compile / wartremoverInspect / wartremoverWarnings ++= Warts.all

name := "wartremover-inspector-test"

scalaVersion := "3.3.7"

wartremoverExcluded += (baseDirectory.value / "src/main/scala/ignore")

ThisBuild / wartremoverCrossVersion := CrossVersion.binary

Compile / wartremoverInspectOutputFile := Some(baseDirectory.value / "warts-main.json")

Test / wartremoverInspectOutputFile := Some(baseDirectory.value / "warts-test.json")
