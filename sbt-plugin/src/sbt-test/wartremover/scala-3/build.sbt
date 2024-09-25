scalaVersion := "3.3.4"

scalacOptions += "-P:wartremover:traverser:org.wartremover.warts.OrTypeLeastUpperBound$All"
