crossScalaVersions := Seq("2.11.12", "2.12.12", "2.12.13", "2.12.14", "2.12.15", "2.13.2", "2.13.3", "2.13.4", "2.13.5", "2.13.7", "3.0.0")

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
  """set crossScalaVersions := Seq("2.11.12", "2.12.15", "2.13.5")""" :: // set latest versions
  _
}
