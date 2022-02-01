import ReleaseTransformations._
import com.jsuereth.sbtpgp.PgpKeys
import xsbti.api.{ClassLike, DefinitionType}
import scala.reflect.NameTransformer
import java.lang.reflect.Modifier

Global / onChangedBuildSource := ReloadOnSourceChanges

// compiler plugin should be fully cross-versioned. e.g.
// - https://github.com/ghik/silencer/issues/31
// - https://github.com/typelevel/kind-projector/issues/15
//
// add more scala versions when found binary and/or source incompatibilities in scala-compiler
lazy val allScalaVersions = Seq(
  "2.11.12",
  "2.12.10",
  "2.12.11",
  "2.12.12",
  "2.12.13",
  "2.12.14",
  "2.12.15",
  "2.13.0",
  "2.13.1",
  "2.13.2",
  "2.13.3",
  "2.13.4",
  "2.13.5",
  "2.13.6",
  "2.13.7",
  "2.13.8",
)

val latestScala211 = settingKey[String]("")
val latestScala212 = settingKey[String]("")
val latestScala213 = settingKey[String]("")

def latest(n: Int, versions: Seq[String]) = {
  val prefix = "2." + n + "."
  prefix + versions.filter(_ startsWith prefix).map(_.drop(prefix.length).toLong).reduceLeftOption(_ max _).getOrElse(
    sys.error(s"not found Scala ${prefix}x version ${versions}")
  )
}

lazy val baseSettings = Def.settings(
  latestScala211 := latest(11, allScalaVersions),
  latestScala212 := latest(12, allScalaVersions),
  latestScala213 := latest(13, allScalaVersions),
  scalacOptions ++= Seq(
    "-deprecation"
  ),
  scalaVersion := latestScala212.value,
)

lazy val commonSettings = Def.settings(
  baseSettings,
  Seq(packageBin, packageDoc, packageSrc).flatMap {
    // include LICENSE file in all packaged artifacts
    inTask(_)(Seq(
      (Compile / mappings) += ((ThisBuild / baseDirectory).value / "LICENSE") -> "LICENSE"
    ))
  },
  organization := "org.wartremover",
  licenses := Seq(
    "The Apache Software License, Version 2.0" ->
      url("http://www.apache.org/licenses/LICENSE-2.0.txt")
  ),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  (Compile / doc / scalacOptions) ++= {
    val base = (LocalRootProject / baseDirectory).value.getAbsolutePath
    val t = sys.process.Process("git rev-parse HEAD").lineStream_!.head
    Seq(
      "-sourcepath",
      base,
      "-doc-source-url",
      "https://github.com/wartremover/wartremover/tree/" + t + "€{FILE_PATH}.scala"
    )
  },
  publishTo := sonatypePublishToBundle.value,
  homepage := Some(url("https://wartremover.org")),
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

commonSettings
publishArtifact := false
releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

val coreId = "core"

def crossSrcSetting(c: Configuration) = {
  (c / unmanagedSourceDirectories) += {
    val dir = (LocalProject(coreId) / baseDirectory).value / "src" / Defaults.nameForSrc(c.name)
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 13 =>
        dir / s"scala-2.13+"
      case _ =>
        dir / s"scala-2.13-"
    }
  }
}

val coreSettings = Def.settings(
  commonSettings,
  name := "wartremover",
  Test / fork := true,
  crossScalaVersions := allScalaVersions,
  libraryDependencies := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 13 =>
        libraryDependencies.value :+ ("org.scala-lang.modules" %% "scala-xml" % "2.0.1" % "test")
      case _ =>
        libraryDependencies.value
    }
  },
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value
  ),
  libraryDependencies ++= {
    Seq("org.scalatest" %% "scalatest" % "3.2.11" % "test")
  },
  pomPostProcess := { node =>
    import scala.xml._
    import scala.xml.transform._
    val strip = new RewriteRule {
      override def transform(n: Node) =
        if ((n \ "groupId").text == "test-macros" && (n \ "artifactId").text.startsWith("test-macros_"))
          NodeSeq.Empty
        else
          n
    }
    new RuleTransformer(strip).transform(node)(0)
  },
  assembly / assemblyOutputPath := file("./wartremover-assembly.jar")
)

lazy val coreCrossBinary = Project(
  id = "core-cross-binary",
  base = file("core-cross-binary")
).settings(
  coreSettings,
  crossSrcSetting(Compile),
  Compile / scalaSource := (core / Compile / scalaSource).value,
  Compile / resourceDirectory := (core / Compile / resourceDirectory).value,
  crossScalaVersions := Seq(latestScala211.value, latestScala212.value, latestScala213.value),
  crossVersion := CrossVersion.binary
)
  .dependsOn(testMacros % "test->compile")


lazy val core = Project(
  id = coreId,
  base = file("core")
).settings(
  coreSettings,
  crossSrcSetting(Compile),
  crossSrcSetting(Test),
  crossScalaVersions := allScalaVersions,
  crossVersion := CrossVersion.full,
  crossTarget := {
    // workaround for https://github.com/sbt/sbt/issues/5097
    target.value / s"scala-${scalaVersion.value}"
  },
  assembly / assemblyOutputPath := file("./wartremover-assembly.jar")
)
  .dependsOn(testMacros % "test->compile")

val wartClasses = Def.task {
  val loader = (core / Test / testLoader).value
  val wartTraverserClass = Class.forName("org.wartremover.WartTraverser", false, loader)
  Tests.allDefs((core / Compile / compile).value).collect{
    case c: ClassLike =>
      val decoded = c.name.split('.').map(NameTransformer.decode).mkString(".")
      c.definitionType match {
        case DefinitionType.Module =>
          decoded + "$"
        case _ =>
          decoded
      }
  }
  .flatMap(c =>
    try {
      List[Class[_]](Class.forName(c, false, loader))
    } catch {
      case _: ClassNotFoundException =>
        Nil
    }
  )
  .filter(c => !Modifier.isAbstract(c.getModifiers) && wartTraverserClass.isAssignableFrom(c))
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
  scriptedBufferLog := false,
  scriptedLaunchOpts ++= {
    val javaVmArgs = {
      import scala.collection.JavaConverters._
      java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.toList
    }
    javaVmArgs.filter(
      a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith)
    )
  },
  scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
  crossScalaVersions := Seq(latestScala212.value),
  (Compile / sourceGenerators) += Def.task {
    val base = (Compile / sourceManaged).value
    val file = base / "wartremover" / "Wart.scala"
    val warts = wartClasses.value
    val expectCount = 41
    assert(
      warts.size == expectCount,
      s"${warts.size} != ${expectCount}. please update build.sbt when add or remove wart"
    )
    val wartsDir = core.base / "src" / "main" / "scala" / "wartremover" / "warts"
    val unsafe = warts.filter(IO.read(wartsDir / "Unsafe.scala") contains _)
    val content =
      s"""package wartremover
         |// Autogenerated code, see build.sbt.
         |final class Wart private[wartremover](val clazz: String) {
         |  override def toString: String = clazz
         |}
         |object Wart {
         |  val PluginVersion: String = "${version.value}"
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
  .enablePlugins(ScriptedPlugin)

lazy val testMacros: Project = Project(
  id = "test-macros",
  base = file("test-macros")
).settings(
  baseSettings,
  crossScalaVersions := allScalaVersions,
  publish / skip := true,
  publishArtifact := false,
  publish := {},
  publishLocal := {},
  PgpKeys.publishSigned := {},
  PgpKeys.publishLocalSigned := {},
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
  )
)
