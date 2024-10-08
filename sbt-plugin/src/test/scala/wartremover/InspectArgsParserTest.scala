package wartremover

import org.scalactic.source.Position
import org.scalatest.BeforeAndAfterAll
import org.scalatest.EitherValues
import org.scalatest.funsuite.AnyFunSuite
import java.io.File
import sbt.complete.Parser
import sbt.io.IO
import sbt.uri
import wartremover.InspectArgsParserTest._
import wartremover.InspectWart.SourceFile
import wartremover.InspectWart.Type
import wartremover.InspectWart.Uri
import wartremover.InspectWart.WartName
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import scala.annotation.tailrec
import scala.util.Using

class InspectArgsParserTest extends AnyFunSuite with EitherValues with BeforeAndAfterAll {
  private[this] lazy val base: File = IO.createTemporaryDirectory

  override def beforeAll(): Unit = {
    val dir1 = new File(base, "dir_1")
    val dir2 = new File(base, "dir_2")
    IO.createDirectory(dir1)
    IO.createDirectory(dir2)
    Seq(
      new File(dir1, "file_1.scala"),
      new File(dir1, "file_2.scala"),
      new File(dir2, "file_3.scala"),
      new File(base, "file_4.scala"),
    ).foreach(x => IO.writeLines(x, Nil))
  }
  override def afterAll(): Unit = {
    super.afterAll()
    IO.delete(base)
  }

  private[this] val parser = InspectArgsParser.get(
    workingDirectory = base.toPath.toAbsolutePath,
    pathFilter = Function.const(true)
  )
  private[this] def parse(input: String): Either[String, Seq[InspectArg]] =
    Parser.parse(input, parser)

  private[this] def r(input: String)(implicit p: Position): Seq[InspectArg] =
    parse(input).value

  private[this] def l(input: String)(implicit p: Position): String =
    parse(input).left.value

  test("failure") {
    assert(l("no_space").startsWith("Expected whitespace character"))
    assert(l(" --error").startsWith("Expected whitespace character"))
  }
  test("only '--'") {
    val x = l(" -- ")
    Seq(
      "Expected '--warn'",
      "Expected '--error'",
      "Expected non-whitespace character",
    ).foreach { e =>
      assert(x.contains(e))
    }
  }
  val expectToken = Seq("file:", "https://", "http://", "non-whitespace character")
  test("empty after '--error' or '--warn'") {
    val x1 = l(" --error ")
    expectToken.foreach { t =>
      x1.contains(s"Expected '${t}'")
    }
    val x2 = l(" --warn ")
    expectToken.foreach { t =>
      x2.contains(s"Expected '${t}'")
    }
  }
  test("repeat '--error'") {
    val x = l(" --error --error")
    expectToken.foreach { t =>
      x.contains(s"Expected '${t}'")
    }
  }
  test("success") {
    assert(r(" foo") == List(InspectArg.Wart(WartName("foo"), Type.Warn)))
    assert(r(" https://example.com") == List(InspectArg.Wart(Uri(uri("https://example.com")), Type.Warn)))
    assert(
      r(" --error https://example.com/1 https://example.com/2") ==
        List(
          InspectArg.Wart(Uri(uri("https://example.com/1")), Type.Err),
          InspectArg.Wart(Uri(uri("https://example.com/2")), Type.Err)
        )
    )

    assert(
      r(" https://example.com/1 file://foo/bar --error https://example.com/2 aaa.bbb") ==
        List(
          InspectArg.Wart(Uri(uri("https://example.com/1")), Type.Warn),
          InspectArg.Wart(SourceFile(new File("/foo/bar").toPath), Type.Warn),
          InspectArg.Wart(Uri(uri("https://example.com/2")), Type.Err),
          InspectArg.Wart(WartName("aaa.bbb"), Type.Err),
        )
    )
  }
  test("completions") {
    def f(input: String, expected: Set[String])(implicit p: Position) = {
      val actual = input.foldLeft(parser)(_ derive _).completions(0).get.map(_.display)
      if (actual.size != expected.size) {
        actual.toList.sorted.map("\"" + _ + "\",").foreach(println)
      }
      assert(actual == expected)
    }

    f(
      " htt",
      Set(
        "p://",
        "ps://",
        "ps://raw.githubusercontent.com/",
        "p:// ",
        "ps:// ",
        "ps://raw.githubusercontent.com/ ",
      )
    )

    f(
      " org.wartremover",
      Set(
        "Any",
        "AnyVal",
        "ArrayEquals",
        "AsInstanceOf",
        "AutoUnboxing",
        "CaseClassPrivateApply",
        "CollectHeadOption",
        "DefaultArguments",
        "DropTakeToSlice",
        "EitherProjectionPartial",
        "Enumeration",
        "Equals",
        "FilterEmpty",
        "FilterHeadOption",
        "FilterSize",
        "FinalCaseClass",
        "FinalVal",
        "ForeachEntry",
        "GetGetOrElse",
        "GetOrElseNull",
        "GlobalExecutionContext",
        "ImplicitConversion",
        "ImplicitParameter",
        "IsInstanceOf",
        "IterableOps",
        "JavaNetURLConstructors",
        "LeakingSealed",
        "ListAppend",
        "ListUnapply",
        "ListUnapplySeq",
        "MapUnit",
        "MutableDataStructures",
        "NoNeedImport",
        "NonUnitStatements",
        "Nothing",
        "Null",
        "ObjectThrowable",
        "Option2Iterable",
        "OptionPartial",
        "Overloading",
        "PlatformDefault",
        "Product",
        "Recursion",
        "RedundantAsInstanceOf",
        "RedundantConversions",
        "RedundantIsInstanceOf",
        "Return",
        "ReverseFind",
        "ReverseIterator",
        "ReverseTakeReverse",
        "ScalaApp",
        "SeqApply",
        "SeqUpdated",
        "Serializable",
        "SizeIs",
        "SizeToLength",
        "SortFilter",
        "SortedMaxMin",
        "SortedMaxMinOption",
        "StringPlusAny",
        "ThreadSleep",
        "Throw",
        "ToString",
        "TripleQuestionMark",
        "TryPartial",
        "Var",
        "While",
      ).map("org.wartremover.warts." + _)
    )

    f(" --e", Set("--error"))

    f(" --w", Set("--warn"))

    f(
      " file:",
      Set(
        "dir_1",
        "dir_1/file_1.scala",
        "dir_1/file_2.scala",
        "dir_2",
        "dir_2/file_3.scala",
        "file_4.scala",
      )
    )

    f(
      " file:.",
      Set(
        "",
        " ",
        "./dir_1",
        "./dir_1/file_1.scala",
        "./dir_1/file_2.scala",
        "./dir_2",
        "./dir_2/file_3.scala",
        "./file_4.scala",
      )
    )
  }

  test("wart names") {
    val v = "3.2.1"
    val jars = coursier.Fetch().addDependencies("org.wartremover" % "wartremover_3" % v).run()
    val jar = jars.find(_.getName == s"wartremover_3-${v}.jar").getOrElse(sys.error(s"not found ${jars}"))
    val classes = WartRemover.getAllClassNamesInJar(jar)
    val exclude = Set(
      "Discard$Try",
      "Discard$Either",
      "Discard$Future",
      "Unsafe",
      // exclude on purpose because maybe does not work following warts with inspector
      "Matchable",
      "OrTypeLeastUpperBound$All",
      "OrTypeLeastUpperBound$Any",
      "OrTypeLeastUpperBound$AnyRef",
      "OrTypeLeastUpperBound$AnyVal",
      "OrTypeLeastUpperBound$Matchable",
      "OrTypeLeastUpperBound$Product",
      "OrTypeLeastUpperBound$Serializable",
    ).map("org.wartremover.warts." + _)
    val result = Using.resource(new URLClassLoader(jars.map(_.toURI.toURL).toArray)) { loader =>
      classes.filter { className =>
        val clazz = Class.forName(className, false, loader)
        val traverserName = "org.wartremover.WartTraverser"

        @tailrec
        def loop(c: Class[?]): Boolean = {
          if (c == null) {
            false
          } else if (c.getName == traverserName) {
            true
          } else {
            loop(c.getSuperclass)
          }
        }

        if (Modifier.isAbstract(clazz.getModifiers)) {
          false
        } else {
          loop(clazz)
        }
      }.map { s =>
        if (s.endsWith("$")) s.dropRight(1) else s
      }.filterNot(_.contains("$anon")).filterNot(exclude)
    } ++ Seq[String](
      "ObjectThrowable"
    ).map(
      "org.wartremover.warts." + _
    )
    assert(result.toSet == InspectArgsParser.scala3warts)
  }
}

object InspectArgsParserTest {
  implicit class StringOps(private val groupId: String) extends AnyVal {
    def %(artifactId: String): coursier.core.Module =
      coursier.core.Module(
        coursier.core.Organization(groupId),
        coursier.core.ModuleName(artifactId),
        Map.empty
      )
  }

  implicit class ModuleOps(private val module: coursier.core.Module) extends AnyVal {
    def %(version: String): coursier.core.Dependency =
      coursier.core.Dependency(module, version)
  }
}
