crossScalaVersions := Seq("2.12.15", "2.13.8", "3.1.2")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
