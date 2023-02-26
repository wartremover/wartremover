package org.wartremover

import org.scalatest.funsuite.AnyFunSuite
import sbt.io.IO
import scala.quoted.Quotes
import scala.tasty.inspector.Inspector
import scala.tasty.inspector.Tasty
import scala.tasty.inspector.TastyInspector

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
  private val allWarts: List[WartTraverser] = List(
    org.wartremover.warts.Any,
    org.wartremover.warts.AnyVal,
    org.wartremover.warts.ArrayEquals,
    org.wartremover.warts.AsInstanceOf,
    org.wartremover.warts.AutoUnboxing,
    org.wartremover.warts.CollectHeadOption,
    org.wartremover.warts.DefaultArguments,
    org.wartremover.warts.DropTakeToSlice,
    org.wartremover.warts.EitherProjectionPartial,
    org.wartremover.warts.Enumeration,
    org.wartremover.warts.Equals,
    org.wartremover.warts.FilterEmpty,
    org.wartremover.warts.FilterHeadOption,
    org.wartremover.warts.FilterSize,
    org.wartremover.warts.FinalCaseClass,
    org.wartremover.warts.FinalVal,
    org.wartremover.warts.ForeachEntry,
    org.wartremover.warts.GetGetOrElse,
    org.wartremover.warts.GetOrElseNull,
    org.wartremover.warts.GlobalExecutionContext,
    org.wartremover.warts.ImplicitConversion,
    org.wartremover.warts.ImplicitParameter,
    org.wartremover.warts.IsInstanceOf,
    org.wartremover.warts.IterableOps,
    org.wartremover.warts.LeakingSealed,
    org.wartremover.warts.ListAppend,
    org.wartremover.warts.ListUnapply,
    org.wartremover.warts.ListUnapplySeq,
    org.wartremover.warts.Matchable,
    org.wartremover.warts.MutableDataStructures,
    org.wartremover.warts.NoNeedImport,
    org.wartremover.warts.NonUnitStatements,
    org.wartremover.warts.Nothing,
    org.wartremover.warts.Null,
    org.wartremover.warts.Option2Iterable,
    org.wartremover.warts.OptionPartial,
    org.wartremover.warts.Overloading,
    org.wartremover.warts.PlatformDefault,
    org.wartremover.warts.Product,
    org.wartremover.warts.RedundantConversions,
    org.wartremover.warts.Return,
    org.wartremover.warts.ReverseFind,
    org.wartremover.warts.ReverseIterator,
    org.wartremover.warts.ReverseTakeReverse,
    org.wartremover.warts.ScalaApp,
    org.wartremover.warts.Serializable,
    org.wartremover.warts.SizeIs,
    org.wartremover.warts.SortFilter,
    org.wartremover.warts.StringPlusAny,
    org.wartremover.warts.ThreadSleep,
    org.wartremover.warts.Throw,
    org.wartremover.warts.ToString,
    org.wartremover.warts.TripleQuestionMark,
    org.wartremover.warts.TryPartial,
    org.wartremover.warts.Var,
    org.wartremover.warts.While,
  )

  private def inspectLibrary(module: coursier.core.Dependency): Map[String, Map[String, Int]] = {
    val jars = coursier.Fetch().addDependencies(module).run()
    jars.map { jar =>
      println("start " + jar)
      val result = IO.withTemporaryDirectory { dir =>
        val tastyFiles = IO.unzip(jar, dir, _ endsWith ".tasty").map(_.getAbsolutePath).toList
        println("tasty files count = " + tastyFiles.size)
        val param = InspectParam(
          tastyFiles = tastyFiles,
          dependenciesClasspath = jars.map(_.getAbsolutePath).toList,
          wartClasspath = Nil,
          errorWarts = Nil,
          warningWarts = allWarts.map(_.fullName),
          exclude = Nil,
          failIfWartLoadError = true,
          outputStandardReporter = true
        )
        inspector.run(param)
      }
      jar.getName -> result.warnings.map(_.wart.replace(packagePrefix, "")).groupBy(identity).map { case (k, v) =>
        k -> v.size
      }
    }.toMap
  }

  test("cats") {
    val result = inspectLibrary("org.typelevel" %% "cats-core" % "2.9.0")
    assert(
      result("cats-kernel_3-2.9.0.jar") === Map(
        ("AsInstanceOf", 5),
        ("Equals", 64),
        ("ForeachEntry", 2),
        ("ImplicitConversion", 1),
        ("ImplicitParameter", 2),
        ("IterableOps", 2),
        ("MutableDataStructures", 24),
        ("OptionPartial", 2),
        ("Overloading", 8),
        ("Return", 12),
        ("SizeIs", 8),
        ("ToString", 1),
        ("Var", 31),
        ("While", 8)
      )
    )
    assert(
      result("cats-core_3-2.9.0.jar") === Map(
        ("AsInstanceOf", 94),
        ("DefaultArguments", 22),
        ("Equals", 197),
        ("FinalVal", 1),
        ("ForeachEntry", 2),
        ("ImplicitConversion", 239),
        ("ImplicitParameter", 10),
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
        ("ToString", 3),
        ("TripleQuestionMark", 2),
        ("Var", 61),
        ("While", 28)
      )
    )
    assert(
      result("scala3-library_3-3.2.1.jar") === Map(
        ("AsInstanceOf", 505),
        ("DefaultArguments", 12),
        ("Equals", 14),
        ("FinalVal", 9),
        ("ImplicitConversion", 13),
        ("ImplicitParameter", 145),
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
