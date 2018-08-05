import ReleaseTransformations._
import com.typesafe.sbt.pgp.PgpKeys._
import com.typesafe.sbt.pgp.PgpSettings.useGpg
import org.wartremover.TravisYaml.travisScalaVersions
import xsbti.api.{ClassLike, DefinitionType}
import scala.reflect.NameTransformer
import java.lang.reflect.Modifier

lazy val commonSettings = Seq(
  organization := "org.wartremover",
  licenses := Seq(
    "The Apache Software License, Version 2.0" ->
      url("http://www.apache.org/licenses/LICENSE-2.0.txt")
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  scalacOptions in (Compile, doc) ++= {
    val base = (baseDirectory in LocalRootProject).value.getAbsolutePath
    val t = sys.process.Process("git rev-parse HEAD").lines_!.head
    Seq(
      "-sourcepath",
      base,
      "-doc-source-url",
      "https://github.com/wartremover/wartremover/tree/" + t + "€{FILE_PATH}.scala"
    )
  },
  scalaVersion := travisScalaVersions.value.last,
  sbtVersion := {
    scalaBinaryVersion.value match {
      case "2.10" => "0.13.17"
      case _      => "1.1.6" // don't update sbt 1.2.0. see https://github.com/wartremover/wartremover/issues/433
    }
  },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  homepage := Some(url("http://wartremover.org")),
  useGpg := false,
  pomExtra :=
    <scm>
      <url>git@github.com:wartremover/wartremover.git</url>
      <connection>scm:git:git@github.com:wartremover/wartremover.git</connection>
    </scm>
    <developers>
      <developer>
        <id>puffnfresh</id>
        <name>Brian McKenna</name>
        <url>http://brianmckenna.org/</url>
      </developer>
      <developer>
        <name>Chris Neveu</name>
        <url>http://chrisneveu.com</url>
      </developer>
    </developers>
)

lazy val root = Project(
  id = "root",
  base = file("."),
  aggregate = Seq(core, sbtPlug)
).settings(commonSettings ++ Seq(
  publishArtifact := false,
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    releaseStepCommandAndRemaining("+test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
): _*).enablePlugins(CrossPerProjectPlugin)

lazy val macroParadise = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v >= 13 =>
      // if scala 2.13.0-M4 or later, macro annotations merged into scala-reflect
      // https://github.com/scala/scala/pull/6606
      Nil
    case _ =>
      Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
  }
)

lazy val core = Project(
  id = "core",
  base = file("core")
).settings(
  commonSettings,
  name := "wartremover",
  fork in Test := true,
  crossScalaVersions := travisScalaVersions.value,
  Seq(Compile, Test).map { scope =>
    unmanagedSourceDirectories in scope += {
      val dir = baseDirectory.value / "src" / Defaults.nameForSrc(scope.name)
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v >= 13 =>
          dir / s"scala-2.13+"
        case _ =>
          dir / s"scala-2.13-"
      }
    }
  },
  libraryDependencies ++= macroParadise.value,
  libraryDependencies := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        libraryDependencies.value :+ ("org.scalamacros" %% "quasiquotes" % "2.0.1")
      case Some((2, v)) if v >= 13 =>
        libraryDependencies.value :+ ("org.scala-lang.modules" %% "scala-xml" % "1.1.0")
      case _ =>
        libraryDependencies.value
    }
  },
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    "org.scalatest" %% "scalatest" % "3.0.6-SNAP1" % "test"
  ),
  assemblyOutputPath in assembly := file("./wartremover-assembly.jar")
)
  .dependsOn(testMacros % "test->compile")
  .enablePlugins(CrossPerProjectPlugin)
  .enablePlugins(TravisYaml)

val wartClasses = Def.task {
  Tests.allDefs((compile in (core, Compile)).value).collect{
    case c: ClassLike =>
      val decoded = c.name.split('.').map(NameTransformer.decode).mkString(".")
      c.definitionType match {
        case DefinitionType.Module =>
          decoded + "$"
        case _ =>
          decoded
      }
  }
  .map(c => Class.forName(c, false, (testLoader in (core, Test)).value))
  .filter(c => !Modifier.isAbstract(c.getModifiers) && Util.isWartClass(c))
  .map(_.getSimpleName.replace("$", ""))
  .filterNot(Set("Unsafe", "ForbidInference")).sorted
}

lazy val sbtPlug: Project = Project(
  id = "sbt-plugin",
  base = file("sbt-plugin")
).settings(
  commonSettings,
  name := "sbt-wartremover",
  sbtPlugin := true,
  ScriptedPlugin.scriptedSettings,
  ScriptedPlugin.scriptedBufferLog := false,
  ScriptedPlugin.scriptedLaunchOpts ++= {
    val javaVmArgs = {
      import scala.collection.JavaConverters._
      java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.toList
    }
    javaVmArgs.filter(
      a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith)
    )
  },
  ScriptedPlugin.scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
  crossScalaVersions := travisScalaVersions.value.filter(v => Seq("2.12", "2.10").exists(v.startsWith)),
  sourceGenerators in Compile += Def.task {
    val base = (sourceManaged in Compile).value
    val file = base / "wartremover" / "Wart.scala"
    val warts = wartClasses.value
    val wartsDir = core.base / "src" / "main" / "scala" / "wartremover" / "warts"
    val unsafe = warts.filter(IO.read(wartsDir / "Unsafe.scala") contains _)
    val content =
      s"""package wartremover
         |// Autogenerated code, see build.sbt.
         |final class Wart private[wartremover](val clazz: String) {
         |  override def toString: String = clazz
         |}
         |object Wart {
         |  private[wartremover] val PluginVersion = "${version.value}"
         |  private[wartremover] lazy val AllWarts = List(${warts mkString ", "})
         |  private[wartremover] lazy val UnsafeWarts = List(${unsafe mkString ", "})
         |  /** A fully-qualified class name of a custom Wart implementing `org.wartremover.WartTraverser`. */
         |  def custom(clazz: String): Wart = new Wart(clazz)
         |  private[this] def w(nm: String): Wart = new Wart(s"org.wartremover.warts.$$nm")
         |""".stripMargin +
        warts.map(w => s"""  val $w = w("${w}")""").mkString("\n") + "\n}\n"
    IO.write(file, content)
    Seq(file)
  }
)
  .enablePlugins(CrossPerProjectPlugin)
  .enablePlugins(TravisYaml)

lazy val testMacros: Project = Project(
  id = "test-macros",
  base = file("test-macros")
).settings(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "macro-compat" % "1.1.1",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
  ),
  libraryDependencies ++= macroParadise.value
).enablePlugins(CrossPerProjectPlugin)
