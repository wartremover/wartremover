crossScalaVersions := Seq("2.12.20", "2.13.14", "3.5.1")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.3.0"
