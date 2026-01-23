Compile / compile / wartremoverWarnings := Warts.all

Test / compile / wartremoverWarnings := Warts.unsafe

Compile / compile / scalacOptions += s"-P:wartremover:profile:wartremover-main-profile-${scalaBinaryVersion.value}.txt"

Test / compile / scalacOptions += s"-P:wartremover:profile:wartremover-test-profile-${scalaBinaryVersion.value}.txt"

def Scala3 = "3.3.7"

scalaVersion := Scala3

crossScalaVersions := Seq(Scala3, "2.13.18")

InputKey[Unit]("check") := Seq("2.13" -> 76, "3" -> 72).foreach { case (v, all) =>
  val mainResult = IO.readLines(file(s"wartremover-main-profile-${v}.txt"))
  val testResult = IO.readLines(file(s"wartremover-test-profile-${v}.txt"))
  assert(mainResult.size == all, mainResult.size)
  assert(testResult.size == 18, testResult.size)
  object IsPosNum {
    def unapply(s: String): Boolean = {
      assert(s.toDouble >= 0)
      true
    }
  }

  val allNames: Set[String] = Warts.all.map(_.clazz).toSet

  Seq(mainResult, testResult).foreach { result =>
    result(0).split(" ").toList match {
      case List("all", IsPosNum()) =>
      // ok
      case other =>
        sys.error(other.toString)
    }
    result.drop(1).map(_.split(" ").toList).foreach {
      case List(x, IsPosNum(), IsPosNum()) =>
        assert(allNames(x), x)
      case other =>
        sys.error(other.toString)
    }
  }
}
