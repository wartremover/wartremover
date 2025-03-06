crossScalaVersions := Seq(
  "2.12.18",
  "2.12.19",
  "2.12.20",
  "2.13.14",
  "2.13.15",
  "2.13.16",
  "3.1.3",
  "3.2.2",
  "3.3.3",
  "3.3.4",
  "3.3.5",
  "3.6.4"
)

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

wartremoverErrors += Wart.NoNeedImport

wartremoverErrors += Wart.CaseClassPrivateApply

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
    """set crossScalaVersions := Seq("2.12.20", "2.13.16", "3.3.5")""" :: // set latest versions
    _
}
