crossScalaVersions := Seq("2.11.12", "2.12.11", "2.12.12", "2.13.1", "2.13.2", "2.13.3", "2.13.4")

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
  """set crossScalaVersions := Seq("2.11.12", "2.12.12", "2.13.4")""" :: // set latest versions
  _
}
