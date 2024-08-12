crossScalaVersions := Seq(
  "2.12.17",
  "2.12.18",
  "2.12.19",
  "2.13.11",
  "2.13.12",
  "2.13.13",
  "2.13.14",
  "3.1.3",
  "3.2.2",
  "3.3.3"
)

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

wartremoverErrors += Wart.NoNeedImport

wartremoverErrors += Wart.CaseClassPrivateApply

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
    """set crossScalaVersions := Seq("2.12.19", "2.13.14", "3.5.0")""" :: // set latest versions
    _
}
