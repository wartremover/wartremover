package wartremover

import sbt.{CrossVersion, settingKey}

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
    val wartremoverCrossVersion = settingKey[CrossVersion]("CrossVersion setting for wartremover")
    val Wart = wartremover.Wart
    val Warts = wartremover.Warts
  }

  import autoImport.wartremoverCrossVersion

  override def globalSettings = Seq(
    wartremoverCrossVersion := CrossVersion.full,
    wartremoverErrors := Nil,
    wartremoverWarnings := Nil,
    wartremoverExcluded := Nil,
    wartremoverClasspaths := Nil
  )

  override lazy val projectSettings = wartremoverSettings
}
