scalaVersion := "3.4.2"

scalacOptions += "-Yexplicit-nulls"

wartremoverErrors ++= Seq(Wart.Null, Wart.FinalVal)
