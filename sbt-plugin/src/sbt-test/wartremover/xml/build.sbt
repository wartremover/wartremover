crossScalaVersions := Seq("2.12.17", "2.13.9", "3.2.0")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
