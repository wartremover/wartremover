name := "wartremover"

organization := "org.brianmckenna"

scalaVersion := "2.10.0"

releaseSettings

// -Ywarn-adapted-args has a bug (see SI-6923). Need to
// use -Yno-adapted-args for it to fully
// work. Also -Ywarn-numeric-widen isn't in warn-wall.
scalacOptions ++= Seq(
  "-Ywarn-numeric-widen",
  "-Yno-adapted-args",
  "-Ywarn-all",
  "-Xfatal-warnings"
)

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

licenses := Seq(
  "The Apache Software License, Version 2.0" ->
    url("http://www.apache.org/licenses/LICENSE-2.0.txt")
)

homepage := Some(url("https://github.com/puffnfresh/wartremover"))

pomExtra := (
  <scm>
    <url>git@github.com:puffnfresh/wartremover.git</url>
    <connection>scm:git:git@github.com:puffnfresh/wartremover.git</connection>
  </scm>
  <developers>
    <developer>
      <id>puffnfresh</id>
      <name>Brian McKenna</name>
      <url>http://brianmckenna.org/</url>
    </developer>
  </developers>)
