crossScalaVersions := Seq("3.4.1", "2.13.14", "2.12.19")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
