crossScalaVersions := Seq("2.13.2", "2.12.10", "2.12.11")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
