crossScalaVersions := Seq("2.12.19", "2.13.13", "3.4.0")

wartremoverWarnings ++= Warts.all

scalacOptions += "-Xfatal-warnings"
