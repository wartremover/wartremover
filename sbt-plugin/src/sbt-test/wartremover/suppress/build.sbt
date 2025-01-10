crossScalaVersions := Seq("2.12.20", "2.13.16", "3.3.4")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
