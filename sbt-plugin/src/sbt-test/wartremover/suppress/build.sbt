crossScalaVersions := Seq("2.12.18", "2.13.12", "3.4.0")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
