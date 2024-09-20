crossScalaVersions := Seq("2.12.20", "2.13.15", "3.3.3")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.3.0"
