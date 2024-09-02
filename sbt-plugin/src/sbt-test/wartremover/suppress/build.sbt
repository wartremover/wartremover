crossScalaVersions := Seq("2.12.20", "2.13.14", "3.3.3")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
