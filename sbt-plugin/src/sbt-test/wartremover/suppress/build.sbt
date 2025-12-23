crossScalaVersions := Seq("2.12.21", "2.13.18", "3.3.7")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
