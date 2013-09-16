import sbtrelease._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
import com.typesafe.sbt.pgp.PgpKeys._
import AssemblyKeys._

name := "wartremover"

organization := "org.brianmckenna"

scalaVersion := "2.10.2"

crossVersion := CrossVersion.full

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

resolvers += Resolver.sonatypeRepo("snapshots")

addCompilerPlugin("org.scala-lang.plugins" % "macro-paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full)

libraryDependencies <++= scalaVersion { scalaVer => Seq(
  "org.scala-lang" % "scala-compiler" % scalaVer,
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
) }

scalacOptions in Test <++= packageBin in Compile map { pluginJar => Seq(
  "-Xplugin:" + pluginJar,
  "-P:wartremover:cp:" + pluginJar.toURI.toURL,
  "-P:wartremover:traverser:org.brianmckenna.wartremover.warts.Unsafe"
) }

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
  </developers>
)
