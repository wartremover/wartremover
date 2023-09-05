crossScalaVersions := Seq("2.12.18", "2.13.11", "3.3.1")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
