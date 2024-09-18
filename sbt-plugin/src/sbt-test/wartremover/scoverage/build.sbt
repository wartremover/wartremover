crossScalaVersions := Seq("3.5.1", "2.13.14", "2.12.20")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
