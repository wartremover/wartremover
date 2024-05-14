crossScalaVersions := Seq("2.12.19", "2.13.14", "3.4.2")

wartremoverErrors ++= Warts.all

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
