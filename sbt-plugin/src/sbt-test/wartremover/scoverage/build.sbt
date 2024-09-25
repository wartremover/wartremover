crossScalaVersions := Seq("3.3.4", "2.13.14", "2.12.20")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
