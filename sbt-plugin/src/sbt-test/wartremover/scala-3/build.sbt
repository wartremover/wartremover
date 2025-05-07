scalaVersion := "3.3.6"

scalacOptions += "-P:wartremover:traverser:org.wartremover.warts.OrTypeLeastUpperBound$All"
