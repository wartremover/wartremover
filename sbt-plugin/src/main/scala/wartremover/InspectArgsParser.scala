package wartremover

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import sbt.complete.Parser
import sbt.complete.Parser.token
import sbt.complete.FileExamples
import sbt.complete.DefaultParsers.*
import wartremover.InspectWart.Type
import java.net.URI

/**
 * derived from sbt-scalafix
 * [[https://github.com/scalacenter/sbt-scalafix/blob/2051bf05f79befc8db66/src/main/scala/scalafix/internal/sbt/ScalafixCompletions.scala]]
 */
private[wartremover] object InspectArgsParser {
  private[this] val typeParser: Parser[Type] = {
    val base: Parser[Type] =
      Space ~> ((token("--warn") ^^^ Type.Warn) | (token("--error") ^^^ Type.Err))

    base.?.map(_.getOrElse(Type.Warn))
  }

  private implicit class ParserOps(private val self: Parser[String]) extends AnyVal {
    def doNotParseSpecialArgs: Parser[String] =
      self.filter(!_.startsWith("--"), x => x)
  }

  def get(workingDirectory: Path): Parser[Seq[InspectArg]] =
    get(workingDirectory, p => Files.exists(p))
  def get(workingDirectory: Path, pathFilter: Path => Boolean): Parser[Seq[InspectArg]] = {
    def toAbsolutePath(path: Path, cwd: Path): Path = {
      if (path.isAbsolute) path
      else cwd.resolve(path)
    }.normalize()

    class AbsolutePathExamples(cwd: Path, prefix: String = "") extends FileExamples(cwd.toFile, prefix) {
      override def withAddedPrefix(addedPrefix: String): FileExamples = {
        val nextPrefix =
          if (addedPrefix.startsWith(".")) addedPrefix
          else s"${prefix}${addedPrefix}"
        val (b, p) = AbsolutePathCompleter.mkBase(nextPrefix, cwd)
        new AbsolutePathExamples(b, p)
      }
    }
    object AbsolutePathCompleter {
      def mkBase(prefix: String, fallback: Path): (Path, String) = {
        val path = toAbsolutePath(Paths.get(prefix), fallback)
        if (prefix.endsWith(File.separator)) {
          path -> ""
        } else if (path.getFileName != null) {
          path.getParent -> path.getFileName.toString
        } else {
          fallback -> ""
        }
      }
    }

    val f1: Parser[InspectWart] = token(Space) ~> (token("file:") ~> token(
      StringBasic.doNotParseSpecialArgs
        .examples(new AbsolutePathExamples(workingDirectory))
        .map { f =>
          toAbsolutePath(Paths.get(f), workingDirectory)
        }
        .filter(pathFilter, x => x)
    )).map { x =>
      InspectWart.SourceFile(x)
    }

    (typeParser ~ (f1 | f2 | f3).+).+.map {
      _.flatMap { case (tpe, values) =>
        values.map(x => InspectArg.Wart(value = x, tpe = tpe))
      }
    }
  }

  private[this] val f2: Parser[InspectWart] = {
    val examples = Seq(
      "https://",
      "https://raw.githubusercontent.com/",
      "http://",
    )
    val head = examples.map(token(_)).reduceLeft(_ | _)
    token(Space) ~> (head ~ token(URIClass)).examples(examples *).map { case (p, uri) =>
      InspectWart.Uri(new URI(s"${p}${uri}"))
    }
  }

  private[this] val scala3warts: Set[String] = {
    val exclude = Set(
      "ExplicitImplicitTypes",
      "JavaConversions",
      "JavaSerializable",
      "PublicInference",
    ).map("org.wartremover.warts." + _)
    wartremover.Warts.all.map(_.clazz).toSet -- exclude
  }

  private[this] val f3: Parser[InspectWart] =
    token(Space) ~> token(NotSpace examples scala3warts).doNotParseSpecialArgs.map { wartName =>
      InspectWart.WartName(wartName)
    }
}
