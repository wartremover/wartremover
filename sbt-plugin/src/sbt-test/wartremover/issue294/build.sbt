crossScalaVersions := Seq("2.11.12", "2.12.7", "2.13.0-M5")

wartremoverErrors += Wart.Serializable

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) =>
      Seq("-Xexperimental")
    case _ =>
      Nil
  }
}
