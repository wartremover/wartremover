package wartremover

import sbt.Configuration
import sbt.Def
import java.io.File
import xsbti.FileConverter

private[wartremover] trait WartRemoverCompat { self: WartRemover.type =>
  private[wartremover] def convertToFile(x: xsbti.VirtualFileRef, fileConverter: FileConverter): File =
    fileConverter.toPath(x).toFile

  private[wartremover] def wartremoverTaskSetting(x: Configuration): Seq[Def.Setting[?]] = Nil
}
