import sbtrelease._
import sbtrelease.ReleasePlugin._
import ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
import com.typesafe.sbt.pgp.PgpKeys._
import sbtassembly.Plugin._

name := "wartremover"

organization := "org.brianmckenna"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

crossVersion := CrossVersion.binary

releaseSettings

assemblySettings

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = st => {
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)
    extracted.runAggregated(publishSigned in Global in ref, st)
  }),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full)

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10)) =>
      libraryDependencies.value :+ ("org.scalamacros" %% "quasiquotes" % "2.0.0")
    case _ =>
      libraryDependencies.value
  }
}

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "2.1.3" % "test"
)

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

homepage := Some(url("https://github.com/typelevel/wartremover"))

pomExtra := (
  <scm>
    <url>git@github.com:typelevel/wartremover.git</url>
    <connection>scm:git:git@github.com:typelevel/wartremover.git</connection>
  </scm>
  <developers>
    <developer>
      <id>puffnfresh</id>
      <name>Brian McKenna</name>
      <url>http://brianmckenna.org/</url>
    </developer>
  </developers>
)
