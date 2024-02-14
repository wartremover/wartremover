crossScalaVersions := Seq("2.12.18", "2.13.12", "3.4.0")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
