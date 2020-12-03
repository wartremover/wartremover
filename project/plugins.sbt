addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.1.1")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.5")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

scalacOptions ++= Seq(
  "-feature",
  "-language:existentials",
  "-deprecation",
)
