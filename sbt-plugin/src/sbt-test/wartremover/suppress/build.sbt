crossScalaVersions := Seq("2.12.20", "2.13.15", "3.5.2")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
