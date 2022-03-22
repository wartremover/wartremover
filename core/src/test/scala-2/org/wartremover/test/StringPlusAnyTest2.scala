package org.wartremover
package test

import org.wartremover.warts.StringPlusAny
import org.scalatest.funsuite.AnyFunSuite

class StringPlusAnyTest2 extends AnyFunSuite with ResultAssertions {
  test("Implicit conversion to string is disabled") {
    val result = WartTestTraverser(StringPlusAny) {
      {} + "lol"
    }
    assertError(result)("Implicit conversion to string is disabled")
  }
}
