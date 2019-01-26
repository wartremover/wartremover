package org.wartremover

import java.util.{Map => JMap, List => JList}
import org.yaml.snakeyaml.Yaml
import sbt._
import sbt.io.Using
import sbt.Def.settingKey
import sbt.Keys.baseDirectory
import scala.collection.JavaConverters._

// Adapted from dwijnand/sbt-travisci

object TravisYaml extends AutoPlugin {
  lazy val travisScalaVersions: SettingKey[Seq[String]] = settingKey("Retrieves scala versions from .travis.yml")

  lazy val travisScalaVersionsSetting: Setting[Seq[String]] =
    travisScalaVersions :=
      Using.fileInputStream(baseDirectory.value / ".travis.yml") { fis =>
        Option((new Yaml).load(fis))
          .collect { case map: JMap[_, _] => map }
          .flatMap(map => Option(map get "scala"))
          .collect {
            case versions: JList[_] => versions.asScala.toList map (_.toString)
            case version: String    => version :: Nil
          }
          .getOrElse(Nil)
      }

  override lazy val buildSettings: Seq[Setting[_]] = Seq(travisScalaVersionsSetting)
}
