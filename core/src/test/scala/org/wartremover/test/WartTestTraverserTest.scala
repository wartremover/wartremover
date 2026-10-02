package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.Unsafe

class WartTestTraverserTest extends AnyFunSuite with ResultAssertions {
  test("WartTestTraverser") {
    def someVariable = Unsafe
    WartTestTraverser(someVariable) {
      Nil
    }
  }
}
