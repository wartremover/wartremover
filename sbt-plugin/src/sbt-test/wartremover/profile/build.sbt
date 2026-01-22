Compile / compile / wartremoverWarnings := Warts.all

Test / compile / wartremoverWarnings := Warts.unsafe

Compile / compile / scalacOptions += "-P:wartremover:profile:wartremover-main-profile.txt"

Test / compile / scalacOptions += "-P:wartremover:profile:wartremover-test-profile.txt"

scalaVersion := "3.3.7"

InputKey[Unit]("check") := {
  val mainResult = IO.readLines(file("wartremover-main-profile.txt"))
  val testResult = IO.readLines(file("wartremover-test-profile.txt"))
  assert(mainResult.size == 72, mainResult.size)
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
