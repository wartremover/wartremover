crossScalaVersions := Seq("2.11.12", "2.12.10", "2.12.11", "2.13.0", "2.13.1")

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
  """set crossScalaVersions := Seq("2.11.12", "2.12.11", "2.13.1")""" :: // set latest versions
  _
}
