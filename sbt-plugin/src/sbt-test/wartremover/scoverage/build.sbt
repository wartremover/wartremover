crossScalaVersions := Seq("3.2.0", "2.13.8", "2.12.17")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
