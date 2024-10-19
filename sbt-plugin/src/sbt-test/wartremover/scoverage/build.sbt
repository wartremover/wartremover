crossScalaVersions := Seq("3.6.1", "2.13.15", "2.12.20")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
