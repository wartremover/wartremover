package wartremover

import java.io.File
import sbt.Def
import sbt.Keys
import xsbti.FileConverter

private[wartremover] trait WartRemoverCompat { self: WartRemover.type =>
  private[wartremover] def convertToFile(x: xsbti.VirtualFileRef, fileConverter: FileConverter): File =
    fileConverter.toPath(x).toFile

  private[wartremover] def toVirtualIdEntry(x: xsbti.VirtualFileRef, fileConverter: FileConverter): String =
    "file:" + x.id

  private[wartremover] def fileToVirtualIdEntry(x: File, fileConverter: FileConverter): String =
    toVirtualIdEntry(fileConverter.toVirtualFile(x.toPath), fileConverter)

  private[wartremover] def virtualIdRoots: Def.Initialize[Set[String]] =
    Def.setting(Keys.rootPaths.value.keySet)

  private[wartremover] def resolveVirtualId(path: String, roots: Set[String], fileConverter: FileConverter): File =
    if (roots.exists(key => path.startsWith(s"$${$key}/"))) fileConverter.toPath(xsbti.VirtualFileRef.of(path)).toFile
    else new File(path)

  private[wartremover] def classpathDigest(
    entries: Seq[xsbti.VirtualFileRef],
    fileConverter: FileConverter
  ): Option[String] = {
    val files = entries.filterNot(_.id.startsWith("${CSR_CACHE}")).flatMap { ref =>
      val f = convertToFile(ref, fileConverter)
      if (f.isDirectory) sbt.io.PathFinder(f).allPaths.get().filter(_.isFile).sortBy(_.getPath)
      else Seq(f)
    }
    Some(sbt.util.Digest.sha256Hash(files.map(x => sbt.util.Digest.sha256Hash(x.toPath))*).hashHexString)
  }

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
