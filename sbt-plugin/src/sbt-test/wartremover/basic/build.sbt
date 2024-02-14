crossScalaVersions := Seq(
  "2.12.16",
  "2.12.17",
  "2.12.18",
  "2.13.9",
  "2.13.10",
  "2.13.11",
  "2.13.12",
  "3.1.3",
  "3.2.2",
  "3.3.2"
)

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

wartremoverErrors += Wart.NoNeedImport

wartremoverErrors += Wart.CaseClassPrivateApply

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
    """set crossScalaVersions := Seq("2.12.18", "2.13.12", "3.3.2")""" :: // set latest versions
    _
}
