crossScalaVersions := Seq(
  "2.11.12",
  "2.12.15",
  "2.12.16",
  "2.12.17",
  "2.12.18",
  "2.13.7",
  "2.13.8",
  "2.13.9",
  "2.13.10",
  "2.13.11",
  "2.13.12",
  "3.0.0"
)

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
    """set crossScalaVersions := Seq("2.11.12", "2.12.19", "2.13.12")""" :: // set latest versions
    _
}
