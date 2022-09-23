crossScalaVersions := Seq("2.13.9", "2.12.15")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
