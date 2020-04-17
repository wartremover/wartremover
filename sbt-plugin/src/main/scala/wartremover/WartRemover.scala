package wartremover

import sbt._

object WartRemover extends sbt.AutoPlugin {
  override def trigger = allRequirements
  object autoImport {
    val wartremoverErrors = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation errors.")
    val wartremoverWarnings = settingKey[Seq[Wart]]("List of Warts that will be reported as compilation warnings.")
    val wartremoverExcluded = taskKey[Seq[File]]("List of files to be excluded from all checks.")
    val wartremoverClasspaths = taskKey[Seq[String]]("List of classpaths for custom Warts")
    val wartremoverCrossVersion = settingKey[CrossVersion]("CrossVersion setting for wartremover")
    val Wart = wartremover.Wart
    val Warts = wartremover.Warts
  }

  override def globalSettings = Seq(
    autoImport.wartremoverCrossVersion := CrossVersion.full,
    autoImport.wartremoverErrors := Nil,
    autoImport.wartremoverWarnings := Nil,
    autoImport.wartremoverExcluded := Nil,
    autoImport.wartremoverClasspaths := Nil
  )

  override lazy val projectSettings = wartremoverSettings
}
