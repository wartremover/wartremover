package wartremover

object WartRemover extends sbt.AutoPlugin {
  override lazy val projectSettings = wartremoverSettings
}
