crossScalaVersions := Seq("2.12.19", "2.13.14", "3.5.0")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
