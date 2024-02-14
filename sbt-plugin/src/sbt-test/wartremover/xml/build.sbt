crossScalaVersions := Seq("2.12.18", "2.13.12", "3.3.2")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
