crossScalaVersions := Seq("2.12.18", "2.13.10", "3.3.0")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
