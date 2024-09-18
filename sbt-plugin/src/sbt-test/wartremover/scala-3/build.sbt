scalaVersion := "3.5.1"

scalacOptions += "-P:wartremover:traverser:org.wartremover.warts.OrTypeLeastUpperBound$All"
