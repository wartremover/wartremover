crossScalaVersions := Seq("2.12.18", "2.13.11", "3.3.0")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.2.0"
