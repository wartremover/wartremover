package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Var

package tobeexcluded {
class ExcludedTest extends FunSuite {
    test("Shouldn't cause any error 'cause it's excluded") {
      val result = WartTestTraverser(Var) {
        var x = 5
        x
      }
      expectResult(List.empty, "result.errors")(result.errors)
      expectResult(List.empty, "result.warnings")(result.warnings)
    }
  }
}

package nottobeexcluded {
  class NotExcludedTest extends FunSuite {
    test("Should cause errors since it's not excluded: cannot use Var") {
      val result = WartTestTraverser(Var) {
        var x = 5
        x
      }
      expectResult(List("var is disabled"), "result.errors")(result.errors)
      expectResult(List.empty, "result.warnings")(result.warnings)
    }
  }
}

