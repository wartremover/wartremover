package org.wartremover
package test

import org.wartremover.warts.Overloading
import org.scalatest.funsuite.AnyFunSuite

class OverloadingTest2 extends AnyFunSuite with ResultAssertions {

  // TODO Scala 3
  test("Overloading is disabled") {
    val result = WartTestTraverser(Overloading) {
      class c {
        def f(i: Int) = {}
        def f(s: String) = {}
        def wait(s: String) = {}
      }
    }
    assertErrors(result)("Overloading is disabled", 3)
  }
}
