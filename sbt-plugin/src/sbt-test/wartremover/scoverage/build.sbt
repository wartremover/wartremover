crossScalaVersions := Seq("3.3.2", "2.13.13", "2.12.19")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
