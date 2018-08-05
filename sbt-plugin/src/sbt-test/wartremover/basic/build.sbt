crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.6", "2.13.0-M4")

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions
