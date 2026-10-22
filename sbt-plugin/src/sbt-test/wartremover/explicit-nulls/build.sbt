scalaVersion := "3.3.9"

scalacOptions += "-Yexplicit-nulls"

wartremoverErrors ++= Seq(Wart.Null, Wart.FinalVal)
