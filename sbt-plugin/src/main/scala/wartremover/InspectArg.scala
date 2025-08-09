package wartremover

import sbt.file
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.net.URI
import scala.io.Source

private[wartremover] sealed abstract class InspectArg extends Product with Serializable

private[wartremover] object InspectArg {
  case class Wart(value: InspectWart, tpe: InspectWart.Type) extends InspectArg

}
private[wartremover] sealed abstract class InspectWart extends Product with Serializable

private[wartremover] object InspectWart {
  sealed abstract class Type extends Product with Serializable
  object Type {
    case object Err extends Type
    case object Warn extends Type
  }
  private[wartremover] sealed abstract class FromSource extends InspectWart {
    def getSourceContents(): Seq[String]
  }
  private def fromFile(x: File): Seq[String] = {
    if (x.isFile) {
      Source.fromFile(x)(using scala.io.Codec.UTF8).getLines().mkString("\n") :: Nil
    } else if (x.isDirectory) {
      x.listFiles(_.isFile)
        .map { f =>
          Source.fromFile(f)(using scala.io.Codec.UTF8).getLines().mkString("\n")
        }
        .toList
    } else {
      throw new FileNotFoundException(x.getAbsolutePath)
    }
  }

  final case class SourceFile(value: Path) extends FromSource {
    def getSourceContents(): Seq[String] = fromFile(value.toFile)

  }
  final case class Uri(value: URI) extends FromSource {
    def getSourceContents(): Seq[String] = {
      value.getScheme match {
        case null =>
          fromFile(file(value.toString))
        case _ =>
          Source.fromURL(value.toURL)(using scala.io.Codec.UTF8).getLines().mkString("\n") :: Nil
      }
    }
  }
  final case class WartName(value: String) extends InspectWart

}
