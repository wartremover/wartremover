crossScalaVersions := Seq(
  "2.12.14",
  "2.12.15",
  "2.12.16",
  "2.12.17",
  "2.13.6",
  "2.13.7",
  "2.13.8",
  "2.13.9",
  "2.13.10",
  "3.1.1",
  "3.1.2",
  "3.1.3",
  "3.2.0",
  "3.2.1",
  "3.2.2",
  "3.3.0"
)

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

wartremoverErrors += Wart.NoNeedImport

wartremoverErrors += Wart.CaseClassPrivateApply

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
    """set crossScalaVersions := Seq("2.12.17", "2.13.10", "3.3.0")""" :: // set latest versions
    _
}
