crossScalaVersions := Seq("2.12.17", "2.13.10", "3.2.1")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
