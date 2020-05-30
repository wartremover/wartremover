// TODO add 2.13.2 test
// https://github.com/scoverage/scalac-scoverage-plugin/pull/279
crossScalaVersions := Seq("2.13.0", "2.12.10", "2.12.11")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
