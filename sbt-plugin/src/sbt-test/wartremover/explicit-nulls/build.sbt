scalaVersion := "3.5.1"

scalacOptions += "-Yexplicit-nulls"

wartremoverErrors ++= Seq(Wart.Null, Wart.FinalVal)
