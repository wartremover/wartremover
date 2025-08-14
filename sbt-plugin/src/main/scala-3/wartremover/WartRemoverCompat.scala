package wartremover

import java.io.File
import xsbti.FileConverter

private[wartremover] trait WartRemoverCompat { self: WartRemover.type =>
  private[wartremover] def convertToFile(x: xsbti.VirtualFileRef, fileConverter: FileConverter): File =
    fileConverter.toPath(x).toFile

  private[wartremover] def packageBinCopy(taskName: String, outputFileName: String): String =
    s"""
       |TaskKey[Unit]("${taskName}") := Def.uncached(
       |  IO.copyFile(
       |    fileConverter.value.toPath((Compile / packageBin).value).toFile,
       |    file("${outputFileName}")
       |  )
       |)
       |""".stripMargin

}
