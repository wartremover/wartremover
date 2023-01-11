crossScalaVersions := Seq("2.12.17", "2.13.10", "3.2.2")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
