crossScalaVersions := Seq("2.11.12", "2.12.12", "2.12.13", "2.13.1", "2.13.2", "2.13.3", "2.13.4")

wartremoverErrors += Wart.Serializable

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) =>
      Seq("-Xexperimental")
    case _ =>
      Nil
  }
}
