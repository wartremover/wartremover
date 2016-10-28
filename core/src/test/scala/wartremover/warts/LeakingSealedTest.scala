package org.wartremover
package test

import org.scalatest.FunSuite
import org.wartremover.warts.LeakingSealed

class LeakingSealedTest extends FunSuite {
  test("Descendants of a sealed parent must be located in the parent's file") {
    val result = WartTestTraverser.applyToFiles(LeakingSealed)(List("LeakingSealed/A.scala", "LeakingSealed/B.scala"))

    assertResult(List("Descendants of a sealed parent must be located in the parent's file"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("LeakingSealed wart obeys SuppressWarnings") {
    val result = WartTestTraverser.applyToFiles(LeakingSealed)(List("LeakingSealed/A.scala", "LeakingSealed/C.scala"))

    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
