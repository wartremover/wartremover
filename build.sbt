import ReleaseTransformations._
import com.jsuereth.sbtpgp.PgpKeys
import xsbti.api.ClassLike
import xsbti.api.DefinitionType
import scala.collection.compat._
import scala.reflect.NameTransformer
import java.lang.reflect.Modifier

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / sbtPluginPublishLegacyMavenStyle := false

def nightlyScala3: String = "3.8.1-RC1-bin-20260113-74ab8e5-NIGHTLY"

// compiler plugin should be fully cross-versioned. e.g.
// - https://github.com/ghik/silencer/issues/31
// - https://github.com/typelevel/kind-projector/issues/15
//
// add more scala versions when found binary and/or source incompatibilities in scala-compiler
lazy val allScalaVersions = Seq(
  "2.12.19",
  "2.12.20",
  "2.12.21",
  "2.13.16",
  "2.13.17",
  "2.13.18",
  "3.3.5",
  "3.3.6",
  "3.3.7",
  "3.4.3",
  "3.5.2",
  "3.6.4",
  "3.7.2",
  "3.7.3",
  "3.7.4",
) ++ {
  if (scala.util.Properties.isJavaAtLeast("17")) {
    List(
      "3.8.0",
      "3.8.1-RC1",
      nightlyScala3
    )
  } else {
    Nil
  }
}

def Scala3forSbt2 = "3.7.4"

def latestScala212 = latest(12, allScalaVersions)
def latestScala213 = latest(13, allScalaVersions)
def latestScala3 = allScalaVersions.filterNot(_ contains "-RC").filter(_ startsWith "3.3.").last // TODO more better way

def latest(n: Int, versions: Seq[String]) = {
  val prefix = "2." + n + "."
  prefix + versions
    .filter(_ startsWith prefix)
    .flatMap(_.drop(prefix.length).toLongOption)
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
  resolvers ++= {
    if (scalaVersion.value == nightlyScala3) {
      Seq(Resolver.scalaNightlyRepository)
    } else {
      Nil
    }
  },
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
)

lazy val commonSettings = Def.settings(
  baseSettings,
  libraryDependencies ++= {
    Seq("org.scalatest" %% "scalatest-funsuite" % "3.2.19" % "test")
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
      url("https://www.apache.org/licenses/LICENSE-2.0.txt")
  ),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  (Compile / doc / scalacOptions) ++= {
    val t = sys.process.Process("git rev-parse HEAD").lineStream_!.head
    if (scalaBinaryVersion.value == "3") {
      Seq(
        "-source-links:github://wartremover/wartremover",
        "-revision",
        t
      )
    } else {
      val base = (LocalRootProject / baseDirectory).value.getAbsolutePath
      Seq(
        "-sourcepath",
        base,
        "-doc-source-url",
        "https://github.com/wartremover/wartremover/tree/" + t + "€{FILE_PATH}.scala"
      )
    }
  },
  publishTo := (if (isSnapshot.value) None else localStaging.value),
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
        <url>https://brianmckenna.org/</url>
      </developer>
      <developer>
        <name>Chris Neveu</name>
        <url>http://chrisneveu.com</url>
      </developer>
    </developers>
)

commonSettings
publishArtifact := false
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publishSigned"),
  releaseStepCommand("sonaRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

val coreSrcDir = Def.settingDyn {
  val p = core.jvm(scalaVersion.value)
  Def.setting(
    (p / projectMatrixBaseDirectory).value.getAbsoluteFile / "src"
  )
}

def crossSrcSetting(c: Configuration) = {
  c / unmanagedSourceDirectories ++= {
    val dir = coreSrcDir.value / Defaults.nameForSrc(c.name)
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
  Test / scalacOptions += {
    val hash = (Compile / sources).value.map { f =>
      sbt.internal.inc.HashUtil.farmHash(f.toPath)
    }.sum
    s"-Dplease-recompile-because-main-source-files-changed-${hash}"
  },
  libraryDependencies ++= {
    Seq(
      "org.scala-lang.modules" %% "scala-xml" % "2.1.0" % "test",
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
  Compile / unmanagedSourceDirectories ++= {
    scalaVersion.value match {
      case VersionNumber(Seq(3, n, _), _, _) =>
        val dir = {
          if (n >= 5) {
            "scala-3.5+"
          } else {
            "scala-3.5-"
          }
        }
        val base = coreSrcDir.value / "main"
        Seq(base / dir)
      case _ =>
        Nil
    }
  },
)

lazy val coreCrossBinary = projectMatrix
  .in(file("core-cross-binary"))
  .withId("core-cross-binary")
  .defaultAxes(VirtualAxis.jvm)
  .disablePlugins(AssemblyPlugin)
  .jvmPlatform(scalaVersions = Seq(latestScala212, latestScala213, latestScala3))
  .settings(
    coreSettings,
    crossSrcSetting(Compile),
    Compile / scalaSource := Def.settingDyn {
      val p = core.jvm(scalaVersion.value)
      Def.setting(
        (p / Compile / scalaSource).value
      )
    }.value,
    Compile / resourceDirectory := Def.settingDyn {
      val p = core.jvm(scalaVersion.value)
      Def.setting(
        (p / Compile / resourceDirectory).value
      )
    }.value,
  )
  .dependsOn(testMacros % "test->compile")

lazy val inspectorCommon = projectMatrix
  .in(file("inspector-common"))
  .withId(
    "inspector-common"
  )
  .defaultAxes(VirtualAxis.jvm)
  .disablePlugins(AssemblyPlugin)
  .jvmPlatform(scalaVersions = Seq(latestScala3, latestScala212))
  .settings(
    commonSettings,
    name := "wartremover-inspector-common",
  )

lazy val inspector = projectMatrix
  .in(file("inspector"))
  .withId("inspector")
  .defaultAxes(VirtualAxis.jvm)
  .disablePlugins(AssemblyPlugin)
  .jvmPlatform(scalaVersions = Seq(latestScala3))
  .settings(
    commonSettings,
    name := "wartremover-inspector",
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
          "org.scala-sbt" %% "io" % "1.10.5" % Test,
          "io.get-coursier" % "coursier" % "2.1.24" % Test cross CrossVersion.for3Use2_13 exclude (
            "org.scala-lang.modules",
            "scala-xml_2.13"
          ),
          "io.github.argonaut-io" %% "argonaut" % "6.3.11",
          "org.scala-lang" %% "scala3-tasty-inspector" % scalaVersion.value % Provided,
        )
      } else {
        Nil
      }
    }
  )
  .dependsOn(
    coreCrossBinary,
    inspectorCommon,
  )

def benchmarkScalaVersion = "3.7.4"

lazy val benchmark = project
  .disablePlugins(AssemblyPlugin)
  .enablePlugins(JmhPlugin)
  .settings(
    baseSettings,
    scalaVersion := benchmarkScalaVersion,
    libraryDependencies += "org.scala-lang" %% "scala3-tasty-inspector" % scalaVersion.value,
    Compile / sourceGenerators += Def.task {
      val exclude = scala2only.toSet
      val dir = (Compile / sourceManaged).value
      def get(key: String): Option[Int] = sys.props.get(key).flatMap(_.toIntOption)
      val allClasses = wartClasses.value.sorted
      val classes = (get("benchmark_total"), get("benchmark_index")) match {
        case (Some(total), Some(index)) =>
          val count = allClasses.size
          val size = (count / total) + {
            if (count % total == 0) 0 else 1
          }
          allClasses.drop(index * size).take(size)
        case _ =>
          allClasses
      }
      classes.flatMap { className =>
        if (exclude(className)) {
          Nil
        } else {
          val f = dir / s"${className}.scala"
          IO.write(
            f,
            s"""package org.wartremover.benchmark
               |
               |class ${className} extends WartremoverBenchmark {
               |  override def wart = org.wartremover.warts.${className}
               |}
               |""".stripMargin
          )
          Seq(f)
        }
      }
    }.taskValue,
    noPublish,
  )
  .dependsOn(
    core.jvm(benchmarkScalaVersion)
  )

lazy val core: sbt.internal.ProjectMatrix = projectMatrix
  .in(file("core"))
  .withId("core")
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(
    scalaVersions = allScalaVersions,
    crossVersion = CrossVersion.full
  )
  .settings(
    coreSettings,
    Test / unmanagedSourceDirectories ++= {
      if (scala.util.Properties.isJavaAtLeast("21")) {
        Seq(coreSrcDir.value / "test" / "jdk21")
      } else {
        Nil
      }
    },
    crossSrcSetting(Compile),
    crossSrcSetting(Test),
    publish / skip := (scalaVersion.value == nightlyScala3),
    crossTarget := {
      // workaround for https://github.com/sbt/sbt/issues/5097
      target.value / s"scala-${scalaVersion.value}"
    },
  )
  .configure(p =>
    if (p.id == "core2_13_18") {
      p.settings(
        assembly / assemblyOutputPath := file("./wartremover-assembly.jar")
      )
    } else {
      p.disablePlugins(AssemblyPlugin)
    }
  )
  .dependsOn(testMacros % "test->compile")

val wartClasses: Def.Initialize[Task[Seq[String]]] = Def.taskDyn {
  val p = core.jvm(latestScala212)
  Def.task {
    val loader = (p / Test / testLoader).value
    val wartTraverserClass = Class.forName("org.wartremover.WartTraverser", false, loader)
    val classes = Tests
      .allDefs((p / Compile / compile).value)
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
          List[Class[?]](Class.forName(c, false, loader))
        } catch {
          case _: ClassNotFoundException =>
            Nil
        }
      )
      .filter(c => !Modifier.isAbstract(c.getModifiers) && wartTraverserClass.isAssignableFrom(c))
      .map(_.getSimpleName.replace("$", ""))
      .filterNot(Set("Unsafe", "ForbidInference", "Matchable"))

    (classes ++ scala2only).distinct.sorted
  }
}

val scala2only = Seq(
  "ExplicitImplicitTypes",
  "JavaConversions",
  "JavaSerializable",
  "PublicInference",
)

val scoverage = "org.scoverage" % "sbt-scoverage" % "2.4.4" % "runtime" // for scala-steward

lazy val sbtPlug: sbt.internal.ProjectMatrix = projectMatrix
  .in(file("sbt-plugin"))
  .withId("sbt-plugin")
  .defaultAxes(VirtualAxis.jvm)
  .disablePlugins(AssemblyPlugin)
  .jvmPlatform(scalaVersions =
    Seq(
      latestScala212,
      Scala3forSbt2,
    )
  )
  .settings(
    commonSettings,
    name := "sbt-wartremover",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" =>
          sbtVersion.value
        case _ =>
          "2.0.0-RC8"
      }
    },
    libraryDependencies ++= {
      scalaBinaryVersion.value match {
        case scalaV @ "2.12" =>
          Seq(
            Defaults.sbtPluginExtra(scoverage, "1.0", scalaV)
          )
        case _ =>
          Nil
      }
    },
    pomPostProcess := { node =>
      import scala.xml.{NodeSeq, Node}
      val rule = new scala.xml.transform.RewriteRule {
        override def transform(n: Node) = {
          if (
            List(
              n.label == "dependency",
              (n \ "groupId").text == scoverage.organization,
              (n \ "artifactId").text.contains(scoverage.name),
            ).forall(identity)
          ) {
            NodeSeq.Empty
          } else if (n.label == "extraDependencyAttributes") {
            NodeSeq.Empty
          } else {
            n
          }
        }
      }
      new scala.xml.transform.RuleTransformer(rule).transform(node)(0)
    },
    packagedArtifacts := {
      val value = packagedArtifacts.value
      val pomFiles = value.values.filter(_.getName.endsWith(".pom")).toList
      assert(pomFiles.size >= 1, pomFiles.map(_.getName))
      pomFiles.foreach { f =>
        assert(!IO.read(f).contains("scoverage"))
      }
      value
    },
    sbtPlugin := true,
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= {
      val javaVmArgs = {
        import scala.collection.JavaConverters._
        java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.toList
      }
      javaVmArgs.filter(a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith))
    },
    conflictWarning := {
      if (scalaBinaryVersion.value == "3") {
        ConflictWarning("warn", Level.Warn, false)
      } else {
        conflictWarning.value
      }
    },
    libraryDependencies += "io.get-coursier" %% "coursier" % "2.1.24" % Test cross CrossVersion.for3Use2_13,
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    scriptedLaunchOpts += ("-Dscoverage.version=" + scoverage.revision),
    TaskKey[Unit]("scriptedTestSbt2") := Def.taskDyn {
      val values = sbtTestDirectory.value
        .listFiles(_.isDirectory)
        .flatMap { dir1 =>
          dir1.listFiles(_.isDirectory).map { dir2 =>
            dir1.getName -> dir2.getName
          }
        }
        .toList
      val log = streams.value.log
      values.foreach { case (dir1, dir2) =>
        val base = sbtTestDirectory.value / dir1 / dir2
        val forSbt2 = base / "test-sbt-2"
        if (forSbt2.isFile) {
          val to = base / "test"
          log.info(s"move ${forSbt2} to ${to}")
          IO.move(forSbt2, to)
        }
      }
      val args = values.map { case (x1, x2) => s"${x1}/${x2}" }
      val arg = args.mkString(" ", " ", "")
      log.info("scripted" + arg)
      scripted.toTask(arg)
    }.value,
    (Compile / sourceGenerators) += Def.task {
      val base = (Compile / sourceManaged).value
      val file = base / "wartremover" / "Wart.scala"
      val warts = wartClasses.value
      val expectCount = 75
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
  )
  .enablePlugins(ScriptedPlugin)

val `sbt-plugin3` = sbtPlug.jvm(Scala3forSbt2).dependsOn(inspectorCommon.jvm(latestScala3))
val `sbt-plugin2_12` = sbtPlug.jvm(latestScala212).dependsOn(inspectorCommon.jvm(latestScala212))

lazy val testMacros: sbt.internal.ProjectMatrix = projectMatrix
  .in(file("test-macros"))
  .withId("test-macros")
  .defaultAxes(VirtualAxis.jvm)
  .disablePlugins(AssemblyPlugin)
  .jvmPlatform(
    scalaVersions = allScalaVersions,
    crossVersion = CrossVersion.full,
  )
  .settings(
    baseSettings,
    noPublish,
    scalaCompilerDependency,
  )

lazy val noPublish = Def.settings(
  publish / skip := true,
  publishArtifact := false,
  publish := {},
  publishLocal := {},
  PgpKeys.publishSigned := {},
  PgpKeys.publishLocalSigned := {},
)
