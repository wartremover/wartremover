import ReleaseTransformations._
import com.jsuereth.sbtpgp.PgpKeys
import xsbti.api.ClassLike
import xsbti.api.DefinitionType
import scala.reflect.NameTransformer
import java.lang.reflect.Modifier

Global / onChangedBuildSource := ReloadOnSourceChanges

// compiler plugin should be fully cross-versioned. e.g.
// - https://github.com/ghik/silencer/issues/31
// - https://github.com/typelevel/kind-projector/issues/15
//
// add more scala versions when found binary and/or source incompatibilities in scala-compiler
lazy val allScalaVersions = Seq(
  "2.12.13",
  "2.12.14",
  "2.12.15",
  "2.12.16",
  "2.12.17",
  "2.13.6",
  "2.13.7",
  "2.13.8",
  "2.13.9",
  "2.13.10",
  "3.1.1",
  "3.1.2",
  "3.1.3",
  "3.2.0",
  "3.2.1",
  "3.2.2",
  "3.3.0-RC3",
)

def latestScala212 = latest(12, allScalaVersions)
def latestScala213 = latest(13, allScalaVersions)
def latestScala3 = allScalaVersions.filterNot(_ contains "-RC").last // TODO more better way

addCommandAlias("SetLatestStableScala3", s"""++ ${latestScala3}! -v""")

def latest(n: Int, versions: Seq[String]) = {
  val prefix = "2." + n + "."
  prefix + versions
    .filter(_ startsWith prefix)
    .map(_.drop(prefix.length).toLong)
    .reduceLeftOption(_ max _)
    .getOrElse(
      sys.error(s"not found Scala ${prefix}x version ${versions}")
    )
}

val scalaCompilerDependency = Def.settings(
  libraryDependencies += {
    if (scalaBinaryVersion.value == "3") {
      "org.scala-lang" %% "scala3-compiler" % scalaVersion.value
    } else {
      "org.scala-lang" % "scala-compiler" % scalaVersion.value
    }
  },
)

lazy val baseSettings = Def.settings(
  scalacOptions ++= Seq(
    "-deprecation"
  ),
  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "2.12" =>
        Seq("-language:higherKinds")
      case _ =>
        Nil
    }
  },
  Test / javaOptions ++= Seq("-Xmx5G"),
  run / fork := true,
  scalaVersion := latestScala212,
)

lazy val commonSettings = Def.settings(
  baseSettings,
  libraryDependencies ++= {
    Seq("org.scalatest" %% "scalatest-funsuite" % "3.2.15" % "test")
  },
  Seq(packageBin, packageDoc, packageSrc).flatMap {
    // include LICENSE file in all packaged artifacts
    inTask(_)(
      Seq(
        (Compile / mappings) += ((ThisBuild / baseDirectory).value / "LICENSE") -> "LICENSE"
      )
    )
  },
  organization := "org.wartremover",
  licenses := Seq(
    "The Apache Software License, Version 2.0" ->
      url("http://www.apache.org/licenses/LICENSE-2.0.txt")
  ),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  (Compile / doc / scalacOptions) ++= {
    if (scalaBinaryVersion.value == "3") {
      Nil // TODO
    } else {
      val base = (LocalRootProject / baseDirectory).value.getAbsolutePath
      val t = sys.process.Process("git rev-parse HEAD").lineStream_!.head
      Seq(
        "-sourcepath",
        base,
        "-doc-source-url",
        "https://github.com/wartremover/wartremover/tree/" + t + "€{FILE_PATH}.scala"
      )
    }
  },
  publishTo := sonatypePublishToBundle.value,
  homepage := Some(url("https://github.com/wartremover/wartremover")),
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
  c / unmanagedSourceDirectories ++= {
    val dir = (LocalProject(coreId) / baseDirectory).value / "src" / Defaults.nameForSrc(c.name)
    PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
        case Some((2, v)) if v <= 12 =>
          dir / s"scala-2.13-"
        case _ =>
          dir / s"scala-2.13+"
      }
      .toList
  }
}

val coreSettings = Def.settings(
  commonSettings,
  name := "wartremover",
  Test / fork := true,
  crossScalaVersions := allScalaVersions,
  Test / scalacOptions += {
    val hash = (Compile / sources).value.map { f =>
      sbt.internal.inc.HashUtil.farmHash(f.toPath)
    }.sum
    s"-Dplease-recompile-because-main-source-files-changed-${hash}"
  },
  Test / scalacOptions ++= {
    if (scalaBinaryVersion.value == "3") {
      Seq("-source:3.0-migration")
    } else {
      Nil
    }
  },
  libraryDependencies ++= {
    Seq(
      "org.scala-lang.modules" %% "scala-xml" % "2.1.0" % "test",
      "org.mockito" % "mockito-core" % "4.11.0" % "test",
    )
  },
  scalaCompilerDependency,
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
  crossScalaVersions := Seq(latestScala212, latestScala213, latestScala3),
  crossVersion := CrossVersion.binary
).dependsOn(testMacros % "test->compile")

lazy val inspectorCommon = Project(
  id = "inspector-common",
  base = file("inspector-common")
).settings(
  commonSettings,
  publish / skip := (scalaBinaryVersion.value == "2.13"),
  crossScalaVersions := Seq(latestScala3, latestScala212),
  name := "wartremover-inspector-common",
)

lazy val inspector = Project(
  id = "inspector",
  base = file("inspector")
).settings(
  commonSettings,
  name := "wartremover-inspector",
  crossScalaVersions := Seq(latestScala3),
  publish / skip := (scalaBinaryVersion.value != "3"),
  Test / fork := true,
  Test / baseDirectory := target.value / "test-base-dir",
  Test / testOptions ++= List(
    Tests.Setup { () =>
      val dir = (Test / baseDirectory).value
      IO.delete(dir)
      dir.mkdirs()
    },
    Tests.Cleanup { () =>
      IO.delete((Test / baseDirectory).value)
    }
  ),
  libraryDependencies ++= {
    if (scalaBinaryVersion.value == "3") {
      Seq(
        "org.scala-sbt" %% "io" % "1.8.0" % Test,
        "io.get-coursier" % "coursier" % "2.1.0" % Test cross CrossVersion.for3Use2_13 exclude ("io.argonaut", "*") exclude ("org.scala-lang.modules", "scala-xml_2.13"),
        "io.argonaut" %% "argonaut" % "6.3.8",
        "org.scala-lang" %% "scala3-tasty-inspector" % scalaVersion.value % Provided,
      )
    } else {
      Nil
    }
  }
).dependsOn(
  coreCrossBinary,
  inspectorCommon,
)

lazy val core = Project(
  id = coreId,
  base = file("core")
).settings(
  coreSettings,
  crossSrcSetting(Compile),
  crossSrcSetting(Test),
  crossScalaVersions := allScalaVersions,
  crossVersion := CrossVersion.full,
  commands += {
    object ToInt {
      def unapply(s: String): Option[Int] = Some(s.toInt)
    }
    Command.args("testPartial", "") { case (state, Seq(ToInt(total), ToInt(index))) =>
      assert(0 <= index, index)
      assert(0 < total, total)
      assert(index < total, (total, index))
      val allVersions = Project.extract(state).get(crossScalaVersions)
      val eachSize = math.ceil(allVersions.size / total.toDouble).toInt
      val values = allVersions.sliding(eachSize, eachSize).toList
      assert(values.size == total, values.toString)
      assert(values.flatten == allVersions, values.toString)
      val result = values(index).flatMap { v =>
        s"++ ${v}!" :: "test" :: Nil
      }.toList
      println(result)
      result ::: state
    }
  },
  Test / sources := {
    val src = (Test / sources).value
    if (scalaBinaryVersion.value != "3") {
      src
    } else if (SemanticSelector(">=3.3.0-RC1").matches(VersionNumber(scalaVersion.value))) {
      // maybe https://github.com/lampepfl/dotty/pull/15642
      val exclude = Set[String](
        "Matchable",
        "AnyVal",
      ).map(_ + "Test.scala")
      src.filterNot(f => exclude(f.getName))
    } else {
      val exclude = Set[String](
        "OrTypeLeastUpperBound",
      ).map(_ + "Test.scala")
      src.filterNot(f => exclude(f.getName))
    }
  },
  crossTarget := {
    // workaround for https://github.com/sbt/sbt/issues/5097
    target.value / s"scala-${scalaVersion.value}"
  },
  assembly / assemblyOutputPath := file("./wartremover-assembly.jar")
).dependsOn(testMacros % "test->compile")

val wartClasses = Def.task {
  val loader = (core / Test / testLoader).value
  val wartTraverserClass = Class.forName("org.wartremover.WartTraverser", false, loader)
  Tests
    .allDefs((core / Compile / compile).value)
    .collect { case c: ClassLike =>
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
    .filterNot(Set("Unsafe", "ForbidInference"))
    .sorted
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
    javaVmArgs.filter(a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith))
  },
  scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
  crossScalaVersions := Seq(latestScala212),
  (Compile / sourceGenerators) += Def.task {
    val base = (Compile / sourceManaged).value
    val file = base / "wartremover" / "Wart.scala"
    val warts = wartClasses.value
    val expectCount = 64
    assert(
      warts.size == expectCount,
      s"${warts.size} != ${expectCount}. please update build.sbt when add or remove wart"
    )
    val wartsDir = core.base / "src" / "main" / "scala" / "org" / "wartremover" / "warts"
    val unsafeSource = IO.read(wartsDir / "Unsafe.scala")
    val unsafe = warts.filter(unsafeSource contains _)
    assert(unsafe.nonEmpty)
    val content =
      s"""package wartremover
         |// Autogenerated code, see build.sbt.
         |final class Wart private[wartremover](val clazz: String) {
         |  override def toString: String = clazz
         |}
         |object Wart {
         |  val PluginVersion: String = "${version.value}"
         |  private[wartremover] lazy val AllWarts: List[Wart] = List(${warts mkString ", "})
         |  private[wartremover] lazy val UnsafeWarts: List[Wart] = List(${unsafe mkString ", "})
         |  /** A fully-qualified class name of a custom Wart implementing `org.wartremover.WartTraverser`. */
         |  def custom(clazz: String): Wart = new Wart(clazz)
         |  private[this] def w(nm: String): Wart = new Wart(s"org.wartremover.warts.$$nm")
         |""".stripMargin +
        warts.map(w => s"""  val $w = w("${w}")""").mkString("\n") + "\n}\n"
    IO.write(file, content)
    Seq(file)
  }
).enablePlugins(ScriptedPlugin)
  .dependsOn(inspectorCommon)

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
  scalaCompilerDependency,
)
