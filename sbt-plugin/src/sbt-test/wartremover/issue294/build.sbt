crossScalaVersions := Seq(
  "2.11.12",
  "2.12.12",
  "2.12.13",
  "2.12.14",
  "2.12.15",
  "2.12.16",
  "2.13.2",
  "2.13.3",
  "2.13.4",
  "2.13.5",
  "2.13.6",
  "2.13.7",
  "2.13.7",
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
