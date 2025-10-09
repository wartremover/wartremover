crossScalaVersions := Seq("2.12.20", "2.13.17", "3.3.7")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.4.0"
