package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

class CompanionTest extends FunSuite {
  test("can use companion objects for case classes") {
    val result = WartTestTraverser(Unsafe) {
      case class Foo(n: Int)
      object Foo {
      }
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
