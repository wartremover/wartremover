crossScalaVersions := Seq("3.3.5", "2.13.16", "2.12.20")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
