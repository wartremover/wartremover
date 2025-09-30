crossScalaVersions := Seq("2.12.20", "2.13.17", "3.3.6")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
