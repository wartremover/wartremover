scalaVersion := "3.2.0"

scalacOptions += "-Yexplicit-nulls"

wartremoverErrors ++= Seq(Wart.Null, Wart.FinalVal)
