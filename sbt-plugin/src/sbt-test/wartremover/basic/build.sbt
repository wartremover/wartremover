crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0-RC3")

wartremoverWarnings ++= Warts.all

wartremoverWarnings += Wart.JavaConversions
