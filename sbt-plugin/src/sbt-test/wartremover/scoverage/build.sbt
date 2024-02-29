crossScalaVersions := Seq("3.4.0", "2.13.13", "2.12.19")

coverageEnabled := true

wartremoverErrors += Wart.NonUnitStatements
wartremoverErrors += Wart.PublicInference
