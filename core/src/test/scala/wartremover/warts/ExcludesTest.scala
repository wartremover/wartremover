package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Any

package tobeexcluded {
class ExcludedTest extends FunSuite {
    test("Shouldn't cause any error 'cause it's excluded") {
      val result = WartTestTraverser(Any) {
        val x = readf1("{0}")
        x
      }
      expectResult(List.empty, "result.errors")(result.errors)
      expectResult(List.empty, "result.warnings")(result.warnings)
    }
  }
}

package nottobeexcluded {
  class NotExcludedTest extends FunSuite {
    test("Should cause errors since it's not excluded: Any can't be inferred") {
      val result = WartTestTraverser(Any) {
        val x = readf1("{0}")
        x
      }
      expectResult(List("Inferred type containing Any"), "result.errors")(result.errors)
      expectResult(List.empty, "result.warnings")(result.warnings)
    }
  }
}

