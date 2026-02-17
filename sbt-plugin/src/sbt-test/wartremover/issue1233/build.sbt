scalaVersion := "3.8.1"

scalacOptions += "-Werror"

wartremoverCrossVersion := CrossVersion.binary

Compile / compile / wartremoverErrors += Wart.AsInstanceOf

wartremoverErrors += Wart.IsInstanceOf

InputKey[Unit]("check") := {
  val x1 = (Compile / compile / scalacOptions).value
  val x2 = (Test / compile / scalacOptions).value
  assert(x1.distinct == x1, x1)
  assert(x2.distinct == x2, x2)
}
