scalaVersion := "3.2.2"

scalacOptions += "-Yexplicit-nulls"

wartremoverErrors ++= Seq(Wart.Null, Wart.FinalVal)
