crossScalaVersions := Seq("2.12.14", "2.12.15", "2.12.16", "2.13.6", "2.13.7", "2.13.8", "3.1.1", "3.1.2", "3.1.3")

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions

commands += Command.command("changeBinary") {
  "set wartremoverCrossVersion := CrossVersion.binary" ::
    """set crossScalaVersions := Seq("2.12.16", "2.13.8", "3.1.3")""" :: // set latest versions
    _
}
