crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.0")

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions
