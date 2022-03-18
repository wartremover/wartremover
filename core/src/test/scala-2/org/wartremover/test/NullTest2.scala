package org.wartremover
package test

import org.wartremover.warts.Null
import org.scalatest.funsuite.AnyFunSuite

// TODO Scala 3 ?
class NullTest2 extends AnyFunSuite with ResultAssertions {
  test("Null wart obeys SuppressWarnings in classes with default arguments") {
    val result = WartTestTraverser(Null) {
      @SuppressWarnings(Array("org.wartremover.warts.Null"))
      class ClassWithArgs(val foo: String = null)
      @SuppressWarnings(Array("org.wartremover.warts.Null"))
      case class CaseClassWithArgs(val foo: String = null)
    }
    assertEmpty(result)
  }
}
