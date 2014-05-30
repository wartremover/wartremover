package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.NoNeedForMonad

class NoNeedForMonadTest extends FunSuite {
  test("Report cases where Applicative is enough") {
    val withWarnings = WartTestTraverser(NoNeedForMonad) {
      for {
        x <- List(1,2,3)
        y <- List(2,3,4)
      } yield x * y

      Option(1).flatMap(i => Option(2).map(j => i + j))
    }
    val noWarnings = WartTestTraverser(NoNeedForMonad) {
      for {
        x <- List(1,2,3)
        y <- x to 3
      } yield x * y

      Option(1).flatMap(i => Option(i + 1).map(j => i + j))
    }

    expectResult(List.empty, "result.errors")(withWarnings.errors)
    expectResult(List(NoNeedForMonad.message, NoNeedForMonad.message), "result.warnings")(withWarnings.warnings)

    expectResult(List.empty, "result.errors")(noWarnings.errors)
    expectResult(List.empty, "result.warnings")(noWarnings.warnings)
  }
}
