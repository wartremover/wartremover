crossScalaVersions := Seq("2.13.7", "2.12.14", "2.11.12")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
