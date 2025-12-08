crossScalaVersions := Seq("2.12.21", "2.13.18", "3.3.7")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.4.0"
