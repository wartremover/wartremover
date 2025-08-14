package wartremover

import sbt.*
import xsbti.FileConverter

private[wartremover] trait WartRemoverCompat { self: WartRemover.type =>

  private[wartremover] implicit class DefOps(self: Def.type) {
    def uncached[A](a: A): A = a
  }

  private[wartremover] def convertToFile(x: File, fileConverter: FileConverter): File =
    x

  private[wartremover] def packageBinCopy(taskName: String, outputFileName: String): String =
    s"""
       |TaskKey[Unit]("${taskName}") := {
       |  IO.copyFile((Compile / packageBin).value, file("${outputFileName}"))
       |}
       |""".stripMargin

}
