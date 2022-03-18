package org.wartremover
package test

import org.wartremover.warts.ExplicitImplicitTypes
import org.scalatest.funsuite.AnyFunSuite
import wartremover.test.ExplicitImplicitTypesTestMacros

class ExplicitImplicitTypesTest extends AnyFunSuite with ResultAssertions {
  test("can't declare implicit vals without a type ascription") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit val foo = 5
    }
    assertError(result)("implicit definitions must have an explicit type ascription")
  }

  test("can't declare implicit defs without a type ascription") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit def foo = 5
      implicit def bar[A] = 5
      implicit def baz(i: Int) = 5
      implicit def qux[I](i: I) = 5
    }
    assertErrors(result)("implicit definitions must have an explicit type ascription", 4)
  }

  test("can't declare implicit vals without a type ascription in macro expansions") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      ExplicitImplicitTypesTestMacros.valsWithoutTypeAscription
    }
    assertErrors(result)("implicit definitions must have an explicit type ascription", 2)
  }

  test("can't declare implicit defs without a type ascription in macro expansions") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      ExplicitImplicitTypesTestMacros.defsWithoutTypeAscription
    }
    assertErrors(result)("implicit definitions must have an explicit type ascription", 4)
  }

  test("can declare implicit classes") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit class Foo(i: Int) {
        def bar = 2
      }
    }
    assertEmpty(result)
  }

  test("can declare implicit vals with a type ascription") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit val foo: Int = 5
      implicit var bar: Int = 5
    }
    assertEmpty(result)
  }

  test("can declare implicit defs with a type ascription") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit def foo: Int = 5
      implicit def bar[A]: Int = 5
      implicit def baz(i: Int): Int = 5
      implicit def qux[I](i: I): Int = 5
    }
    assertEmpty(result)
  }

  test("can declare implicit vals with a type ascription in macro expansions") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      ExplicitImplicitTypesTestMacros.defsWithTypeAscription
    }
    assertEmpty(result)
  }

  test("can declare implicit defs with a type ascription in macro expansions") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      ExplicitImplicitTypesTestMacros.defsWithTypeAscription
    }
    assertEmpty(result)
  }

  test("can declare implicit arguments") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      Option(1).map { implicit i =>
        i
      }
    }
    assertEmpty(result)
  }

  test("can declare non-implicit vals") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      val foo = 5
    }
    assertEmpty(result)
  }

  test("can declare non-implicit defs") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      def foo = 5
    }
    assertEmpty(result)
  }

  test("can declare backticked implicit defs") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit def `foo`: Int = 5
      implicit val `foobar`: String = "5"
      implicit def `foo bar`: Long = 5L
    }
    assertEmpty(result)
  }

  test("ExplicitImplicitTypes wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes"))
      implicit val foo = 5

      @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes"))
      implicit def bar = 5
    }
    assertEmpty(result)
  }
}
