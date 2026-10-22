crossScalaVersions := Seq("3.3.9", "2.13.17", "2.12.20")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
