crossScalaVersions := Seq("3.2.2", "2.13.11", "2.12.18")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
