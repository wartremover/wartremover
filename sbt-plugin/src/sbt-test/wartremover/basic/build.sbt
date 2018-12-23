crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8", "2.13.0-M5")

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions
