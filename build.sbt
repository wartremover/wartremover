scalaVersion := "2.10.0"

// -Ywarn-adapted-args has a bug (see SI-6923). Need to
// use -Yno-adapted-args for it to fully
// work. Also -Ywarn-numeric-widen isn't in warn-wall.
scalacOptions ++= Seq(
  "-Ywarn-numeric-widen",
  "-Yno-adapted-args",
  "-Ywarn-all",
  "-Xfatal-warnings"
)
