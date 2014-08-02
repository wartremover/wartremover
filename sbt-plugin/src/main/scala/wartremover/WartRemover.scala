package wartremover

object WartRemover extends sbt.AutoPlugin {
  override def trigger = allRequirements
  object autoImport {
    // FIXME: all definitions should go here and not in
    //   `package object wartremover`; but for that we
    //   need to wait for sbt<0.13.5 to go out of use
    val wartremoverErrors = wartremover.wartremoverErrors
    val wartremoverWarnings = wartremover.wartremoverWarnings
    val wartremoverExcluded = wartremover.wartremoverExcluded
    val wartremoverClasspaths = wartremover.wartremoverClasspaths
    val Wart = wartremover.Wart
    val Warts = wartremover.Warts
  }
  override lazy val projectSettings = wartremoverSettings
}
