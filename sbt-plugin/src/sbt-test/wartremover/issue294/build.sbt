crossScalaVersions := Seq(
  "2.11.12",
  "2.12.15",
  "2.12.16",
  "2.12.17",
  "2.12.18",
  "2.13.7",
  "2.13.8",
  "2.13.9",
  "2.13.10",
  "2.13.11",
  "2.13.12",
  "3.0.0"
)

wartremoverErrors += Wart.Serializable

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) =>
      Seq("-Xexperimental")
    case _ =>
      Nil
  }
}
