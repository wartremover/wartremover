crossScalaVersions := Seq("2.12.18", "2.13.11", "3.3.1")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
