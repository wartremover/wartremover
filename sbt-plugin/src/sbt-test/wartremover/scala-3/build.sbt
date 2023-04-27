scalaVersion := "3.3.0-RC5"

scalacOptions += "-P:wartremover:traverser:org.wartremover.warts.OrTypeLeastUpperBound$All"
