crossScalaVersions := Seq("2.12.20", "2.13.16", "3.3.6")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.4.0"
