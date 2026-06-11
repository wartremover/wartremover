crossScalaVersions := Seq("3.3.8", "2.13.17", "2.12.20")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
