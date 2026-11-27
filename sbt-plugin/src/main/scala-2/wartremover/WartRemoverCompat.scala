package wartremover

import sbt.*
import xsbti.FileConverter

private[wartremover] trait WartRemoverCompat { self: WartRemover.type =>

  private[wartremover] implicit class DefOps(self: Def.type) {
    def uncached[A](a: A): A = a
  }

  private[wartremover] def convertToFile(x: File, fileConverter: FileConverter): File =
    x

  private[wartremover] def toVirtualIdEntry(x: File, fileConverter: FileConverter): String =
    x.toURI.toString

  private[wartremover] def fileToVirtualIdEntry(x: File, fileConverter: FileConverter): String =
    toVirtualIdEntry(x, fileConverter)

  private[wartremover] def classpathDigest(entries: Seq[File], fileConverter: FileConverter): Option[String] =
    None

  private[wartremover] def virtualIdRoots: Def.Initialize[Set[String]] =
    Def.setting(Set.empty[String])

  private[wartremover] def resolveVirtualId(path: String, roots: Set[String], fileConverter: FileConverter): File =
    new File(path)

  private[wartremover] def packageBinCopy(taskName: String, outputFileName: String): String =
    s"""
       |TaskKey[Unit]("${taskName}") := {
       |  IO.copyFile((Compile / packageBin).value, file("${outputFileName}"))
       |}
       |""".stripMargin

}
