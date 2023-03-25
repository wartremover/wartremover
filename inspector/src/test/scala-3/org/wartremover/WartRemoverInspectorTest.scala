package org.wartremover

import java.io.File
import org.scalatest.funsuite.AnyFunSuite
import sbt.io.IO
import scala.io.Source
import scala.quoted.Quotes
import scala.tasty.inspector.Inspector
import scala.tasty.inspector.Tasty
import scala.tasty.inspector.TastyInspector
import scala.sys.process.Process

class WartRemoverInspectorTest extends AnyFunSuite {
  extension (groupId: String) {
    def %(artifactId: String): coursier.core.Module =
      coursier.core.Module(
        coursier.core.Organization(groupId),
        coursier.core.ModuleName(artifactId),
        Map.empty
      )

    def %%(artifactId: String): coursier.core.Module =
      %(artifactId + "_3")
  }

  extension (module: coursier.core.Module) {
    def %(version: String): coursier.core.Dependency =
      coursier.core.Dependency(module, version)
  }

  private val inspector = new WartRemoverInspector
  private def packagePrefix = "org.wartremover.warts."
  private val allWarts: List[Class[?]] = {
    val suffix = ".class"
    val exclude: Set[Class[?]] = Set[org.wartremover.WartTraverser](
      org.wartremover.warts.Recursion, // too slow?
      org.wartremover.warts.Unsafe,
      org.wartremover.warts.OrTypeLeastUpperBound.Any,
      org.wartremover.warts.OrTypeLeastUpperBound.AnyRef,
      org.wartremover.warts.OrTypeLeastUpperBound.Matchable,
      org.wartremover.warts.OrTypeLeastUpperBound.Product,
      org.wartremover.warts.OrTypeLeastUpperBound.Serializable,
    ).map(_.getClass: Class[?])
    val sample = org.wartremover.warts.Any.getClass
    val values = Source
      .fromInputStream(
        sample.getProtectionDomain.getClassLoader.getResourceAsStream(
          sample.getPackageName.replace('.', '/')
        )
      )
      .getLines
      .filter(_.endsWith(suffix))
      .map(_.dropRight(suffix.length))
      .map { className =>
        Class.forName(packagePrefix + className)
      }
      .filter { clazz =>
        !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers)
      }
      .filter { clazz =>
        Iterator
          .unfold[Class[?], Class[?]](clazz)(c => Option(c).map(x => c -> x.getSuperclass))
          .exists(_ == classOf[WartTraverser])
      }
      .filterNot(exclude)
      .toList
    assert(values.size == 61)
    values
  }

  final case class Repo(githubUser: String, githubName: String, ref: String) {
    def cloneTo(dir: File): Unit = {
      val args = Seq[String](
        "git",
        "-c",
        "advice.detachedHead=false",
        "clone",
        s"https://github.com/${githubUser}/${githubName}.git",
        "-b",
        ref,
        "--depth",
        "1",
        dir.getAbsolutePath
      )
      println(s"run '${args.mkString(" ")}'")
      Process(args).!
    }
    def withSources[A](dir: File): (() => A) => A = { action =>
      try {
        IO.delete(dir)
        cloneTo(dir)
        action.apply()
      } finally {
        IO.delete(dir)
      }
    }
  }

  private def inspectLibrary(
    module: coursier.core.Dependency,
    sources: Map[String, Repo]
  ): Map[String, Map[String, Int]] = {
    val jars = coursier.Fetch().addDependencies(module).run()
    jars.map { jar =>
      println("start " + jar)
      val result = IO.withTemporaryDirectory { dir =>
        sources
          .get(jar.getName)
          .match {
            case Some(repo) =>
              repo.withSources[InspectResult](new File("."))
            case None =>
              println(s"source not found for = ${jar.getName}")
              (a: (() => InspectResult)) => a.apply()
          }
          .apply { () =>
            val tastyFiles = IO.unzip(jar, dir, _ endsWith ".tasty").map(_.getAbsolutePath).toList
            println("tasty files count = " + tastyFiles.size)
            val param = InspectParam(
              tastyFiles = tastyFiles,
              dependenciesClasspath = jars.map(_.getAbsolutePath).toList,
              wartClasspath = Nil,
              errorWarts = Nil,
              warningWarts = allWarts.map(_.getName.dropRight(1)),
              exclude = Nil,
              failIfWartLoadError = true,
              outputStandardReporter = true
            )
            inspector.run(param)
          }
      }
      jar.getName -> result.warnings.map(_.wart.replace(packagePrefix, "")).groupBy(identity).map { case (k, v) =>
        k -> v.size
      }
    }.toMap
  }

  test("cats") {
    val catsVersion = "2.9.0"
    val catsRepo = Repo(
      githubUser = "typelevel",
      githubName = "cats",
      ref = s"v${catsVersion}"
    )
    val scala3version = "3.2.1"
    val result = inspectLibrary(
      "org.typelevel" %% "cats-core" % catsVersion,
      Map(
        s"cats-kernel_3-${catsVersion}.jar" -> catsRepo,
        s"cats-core_3-${catsVersion}.jar" -> catsRepo,
        s"scala3-library_3-${scala3version}.jar" -> Repo("lampepfl", "dotty", scala3version)
      )
    )
    assert(
      result(s"cats-kernel_3-${catsVersion}.jar") === Map(
        ("AsInstanceOf", 5),
        ("Equals", 64),
        ("ForeachEntry", 2),
        ("ImplicitParameter", 2),
        ("IsInstanceOf", 2),
        ("IterableOps", 2),
        ("MutableDataStructures", 24),
        ("OptionPartial", 2),
        ("Overloading", 8),
        ("Return", 12),
        ("SizeIs", 8),
        ("Throw", 3),
        ("ToString", 1),
        ("Var", 31),
        ("While", 8)
      )
    )
    assert(
      result(s"cats-core_3-${catsVersion}.jar") === Map(
        ("AsInstanceOf", 86),
        ("DefaultArguments", 22),
        ("Equals", 197),
        ("FinalVal", 1),
        ("ForeachEntry", 2),
        ("ImplicitConversion", 239),
        ("ImplicitParameter", 10),
        ("IsInstanceOf", 3),
        ("IterableOps", 60),
        ("LeakingSealed", 7),
        ("ListAppend", 3),
        ("MutableDataStructures", 99),
        ("Null", 19),
        ("Option2Iterable", 1),
        ("OptionPartial", 22),
        ("Overloading", 44),
        ("Return", 8),
        ("SizeIs", 3),
        ("StringPlusAny", 1),
        ("Throw", 13),
        ("ToString", 3),
        ("TripleQuestionMark", 2),
        ("Var", 61),
        ("While", 28)
      )
    )
    assert(
      result(s"scala3-library_3-${scala3version}.jar") === Map(
        ("AsInstanceOf", 176),
        ("DefaultArguments", 12),
        ("Equals", 14),
        ("FinalVal", 9),
        ("ImplicitConversion", 13),
        ("ImplicitParameter", 145),
        ("IsInstanceOf", 46),
        ("IterableOps", 2),
        ("LeakingSealed", 2),
        ("MutableDataStructures", 20),
        ("Null", 1),
        ("Option2Iterable", 5),
        ("OptionPartial", 3),
        ("Overloading", 115),
        ("RedundantConversions", 8),
        ("Return", 5),
        ("SizeIs", 2),
        ("StringPlusAny", 2),
        ("Throw", 32),
        ("ToString", 2),
        ("TripleQuestionMark", 11),
        ("Var", 18),
        ("While", 12)
      )
    )
    assert(result("scala-library-2.13.10.jar") === Map.empty)
    assert(result.size === 4)
  }
}
