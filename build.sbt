import ReleaseTransformations._
import com.typesafe.sbt.pgp.PgpKeys._
import com.typesafe.sbt.pgp.PgpSettings.useGpg
import org.wartremover.TravisYaml.travisScalaVersions
import xsbti.api.{ClassLike, DefinitionType}
import scala.reflect.NameTransformer
import java.lang.reflect.Modifier

lazy val baseSettings = Def.settings(
  scalacOptions ++= Seq(
    "-deprecation"
  ),
  scalaVersion := {
    val v = travisScalaVersions.value
    v.find(_.startsWith("2.12")).getOrElse(sys.error(s"not found Scala 2.12.x version $v"))
  }
)

lazy val commonSettings = Def.settings(
  baseSettings,
  Seq(packageBin, packageDoc, packageSrc).flatMap {
    // include LICENSE file in all packaged artifacts
    inTask(_)(Seq(
      mappings in Compile += ((baseDirectory in ThisBuild).value / "LICENSE") -> "LICENSE"
    ))
  },
  organization := "org.wartremover",
  licenses := Seq(
    "The Apache Software License, Version 2.0" ->
      url("http://www.apache.org/licenses/LICENSE-2.0.txt")
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  scalacOptions in (Compile, doc) ++= {
    val base = (baseDirectory in LocalRootProject).value.getAbsolutePath
    val t = sys.process.Process("git rev-parse HEAD").lineStream_!.head
    Seq(
      "-sourcepath",
      base,
      "-doc-source-url",
      "https://github.com/wartremover/wartremover/tree/" + t + "€{FILE_PATH}.scala"
    )
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
  setNextVersion,
  commitNextVersion,
  pushChanges
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
  libraryDependencies := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 13 =>
        libraryDependencies.value :+ ("org.scala-lang.modules" %% "scala-xml" % "1.2.0" % "test")
      case _ =>
        libraryDependencies.value
    }
  },
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value
  ),
  libraryDependencies ++= {
    Seq("org.scalatest" %% "scalatest" % "3.0.8-RC3" % "test")
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
  assemblyOutputPath in assembly := file("./wartremover-assembly.jar")
)
  .dependsOn(testMacros % "test->compile")
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
  .flatMap(c =>
    try {
      List[Class[_]](Class.forName(c, false, (testLoader in (core, Test)).value))
    } catch {
      case _: ClassNotFoundException =>
        Nil
    }
  )
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
  crossScalaVersions := travisScalaVersions.value.filter(_ startsWith "2.12"),
  sourceGenerators in Compile += Def.task {
    val base = (sourceManaged in Compile).value
    val file = base / "wartremover" / "Wart.scala"
    val warts = wartClasses.value
    val expectCount = 36
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
  .enablePlugins(TravisYaml)

lazy val testMacros: Project = Project(
  id = "test-macros",
  base = file("test-macros")
).settings(
  baseSettings,
  crossScalaVersions := travisScalaVersions.value,
  skip in publish := true,
  publishArtifact := false,
  publish := {},
  publishLocal := {},
  publishSigned := {},
  publishLocalSigned := {},
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
  )
).enablePlugins(TravisYaml)
