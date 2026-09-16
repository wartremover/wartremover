crossScalaVersions := Seq(
  "2.12.20",
  "2.12.21",
  "2.13.17",
  "2.13.18",
  "3.3.7",
  "3.3.8",
  "3.8.4",
  "3.9.0",
  "3.10.0-RC2",
)

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

wartremoverErrors += Wart.NoNeedImport

wartremoverErrors += Wart.CaseClassPrivateApply

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" :: _
}
