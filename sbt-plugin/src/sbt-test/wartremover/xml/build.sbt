crossScalaVersions := Seq("2.12.16", "2.13.8", "3.2.0")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
