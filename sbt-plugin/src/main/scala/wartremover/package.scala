import sbt._
import Keys._
import wartremover.WartRemover.autoImport

package object wartremover {
  @deprecated(message = "will be removed. use wartremover.WartRemover.autoImport.wartremoverErrors instead", since = "2.4.7")
  val wartremoverErrors = autoImport.wartremoverErrors
  @deprecated(message = "will be removed. use wartremover.WartRemover.autoImport.wartremoverWarnings instead", since = "2.4.7")
  val wartremoverWarnings = autoImport.wartremoverErrors
  @deprecated(message = "will be removed. use wartremover.WartRemover.autoImport.wartremoverExcluded instead", since = "2.4.7")
  val wartremoverExcluded = autoImport.wartremoverErrors
  @deprecated(message = "will be removed. use wartremover.WartRemover.autoImport.wartremoverClasspaths instead", since = "2.4.7")
  val wartremoverClasspaths = autoImport.wartremoverErrors

  @deprecated(message = "will be removed. use wartremover.WartRemover.projectSettings", since = "2.4.7")
  lazy val wartremoverSettings: Seq[sbt.Def.Setting[_]] =
    wartremover.WartRemover.projectSettings

  @deprecated(message = "will be removed. use wartremover.WartRemover.derive", since = "2.4.7")
  private[wartremover] def derive[T](s: Setting[T]): Setting[T] =
    wartremover.WartRemover.derive[T](s)
}
