package org.wartremover
package test

import org.wartremover.warts.Unsafe
import org.scalatest.funsuite.AnyFunSuite

class WartTestTraverserTest extends AnyFunSuite with ResultAssertions {
  test("WartTestTraverser") {
    def someVariable = Unsafe
    WartTestTraverser(someVariable) {
      Nil
    }
  }
}
