crossScalaVersions := Seq("2.12.21", "3.8.1", "3.3.7")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.4.0"
