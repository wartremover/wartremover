package org.wartremover
package test

import org.wartremover.warts.AsInstanceOf
import org.scalatest.funsuite.AnyFunSuite

class AsInstanceOfTest2 extends AnyFunSuite with ResultAssertions {
  test("issue 264 TypeTag") {
    val result = WartTestTraverser(AsInstanceOf) {
      import scala.reflect.runtime.universe.TypeTag

      def takesTypeTag[A: TypeTag](a: A): String = {
        val tt = implicitly[TypeTag[A]]
        s"The tt of A is $tt"
      }

      def exerciseIt(): String = {
        takesTypeTag("Hello")
      }
    }
    assertEmpty(result)
  }
}
