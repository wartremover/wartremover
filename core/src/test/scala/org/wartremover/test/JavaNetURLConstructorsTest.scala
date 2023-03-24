package org.wartremover
package test

import org.wartremover.warts.JavaNetURLConstructors
import org.scalatest.funsuite.AnyFunSuite
import java.net.URL

class JavaNetURLConstructorsTest extends AnyFunSuite with ResultAssertions {
  test("java.net.URL constructors disabled") {
    List(
      WartTestTraverser(JavaNetURLConstructors) {
        new java.net.URL("https://example.com")
      },
      WartTestTraverser(JavaNetURLConstructors) {
        new URL("https", "example.com", 12345, "foo")
      },
      WartTestTraverser(JavaNetURLConstructors) {
        new URL("https", "example.com", 12345, "foo", null)
      },
      WartTestTraverser(JavaNetURLConstructors) {
        def x: URL = ???
        new URL(x, "a")
      },
      WartTestTraverser(JavaNetURLConstructors) {
        def x: URL = ???
        new URL(x, "b", null)
      },
    ).foreach { result =>
      assertError(result)(JavaNetURLConstructors.message)
    }
  }

  test("java.net.URL wart obeys SuppressWarnings") {
    val result = WartTestTraverser(JavaNetURLConstructors) {
      @SuppressWarnings(Array("org.wartremover.warts.JavaNetURLConstructors"))
      def x1 = new URL("https://example.com")
    }
    assertEmpty(result)
  }
}
