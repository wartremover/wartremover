crossScalaVersions := Seq("2.12.19", "2.13.14", "3.4.1")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
