crossScalaVersions := Seq(
  "2.12.19",
  "2.12.20",
  "2.12.21",
  "2.13.16",
  "2.13.17",
  "2.13.18",
  "3.3.5",
  "3.3.6",
  "3.3.7",
  "3.6.4",
  "3.7.4"
)

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

wartremoverErrors += Wart.NoNeedImport

wartremoverErrors += Wart.CaseClassPrivateApply

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
    """set crossScalaVersions := Seq("2.12.21", "3.8.1", "3.3.7")""" :: // set latest versions
    _
}
