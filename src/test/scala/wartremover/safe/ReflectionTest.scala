package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

class ReflectionTest extends FunSuite {
  import reflect.macros.Context

  test("can use typeOf") {
    val result = WartTestTraverser(Unsafe) {
      def f(c: Context) {
        val t = c.universe.typeOf[String]
      }
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can use Expr[T](Apply(...))") {
    val result = WartTestTraverser(Unsafe) {
      def f(c: Context) {
        import c.universe._
        val e = c.Expr[String](Apply(???, List()))
      }
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

}
