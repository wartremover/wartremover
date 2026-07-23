addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")

// for homebrew
// https://github.com/Homebrew/homebrew-core/blob/a6f4331fd257cb0fbb7475b535142a41b2353286/Formula/w/wartremover.rb
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.4.0")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.5.0")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.8")

addSbtPlugin("com.eed3si9n" % "sbt-salad-days" % "0.2.0")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

scalacOptions ++= Seq(
  "-feature",
  "-language:existentials",
  "-language:implicitConversions",
  "-deprecation",
)

libraryDependencies += "com.github.xuwei-k" %% "scala-version-from-sbt-version" % "0.1.0"
