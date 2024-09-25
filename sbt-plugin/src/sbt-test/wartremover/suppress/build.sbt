crossScalaVersions := Seq("2.12.20", "2.13.15", "3.3.4")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
