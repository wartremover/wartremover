addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.11.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")

// for homebrew
// https://github.com/Homebrew/homebrew-core/blob/a6f4331fd257cb0fbb7475b535142a41b2353286/Formula/w/wartremover.rb
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

scalacOptions ++= Seq(
  "-feature",
  "-language:existentials",
  "-deprecation",
)

if (sys.env.isDefinedAt("GITHUB_ACTION")) {
  Def.settings(
    addSbtPlugin("net.virtual-void" % "sbt-hackers-digest" % "0.1.2")
  )
} else {
  Nil
}
