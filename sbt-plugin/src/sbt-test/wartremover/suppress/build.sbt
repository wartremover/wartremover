crossScalaVersions := Seq("2.12.18", "2.13.12", "3.3.2")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
