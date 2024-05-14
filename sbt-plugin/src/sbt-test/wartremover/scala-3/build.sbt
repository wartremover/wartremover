scalaVersion := "3.4.2"

scalacOptions += "-P:wartremover:traverser:org.wartremover.warts.OrTypeLeastUpperBound$All"
