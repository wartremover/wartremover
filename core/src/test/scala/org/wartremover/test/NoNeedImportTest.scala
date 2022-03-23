package org.wartremover.test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.NoNeedImport

class NoNeedImportTest extends AnyFunSuite with ResultAssertions {
  test("`import scala.util.{ Try, _ }` is disabled") {
    val result = WartTestTraverser(NoNeedImport) {
      import scala.util.{Try, _}
    }
    assertError(result)("The wildcard import exists. Remove other explicitly names of the `import`.")
  }
  test("`import scala.util.{ Try => _ }` is disabled") {
    val result = WartTestTraverser(NoNeedImport) {
      import scala.util.{Try => _}
    }
    assertError(result)("Import into the wildcard(`something => _`) is meaningless. Remove it.")
  }
  test("`import scala.util.{ Try, Success => MySuccess, _ }` is disabled") {
    val result = WartTestTraverser(NoNeedImport) {
      import scala.util.{Try, Success => MySuccess, _}
    }
    assertError(result)("The wildcard import exists. Remove other explicitly names of the `import`.")
  }
  test("`import scala.util.{ Try => _ , _ }` can be used") {
    val result = WartTestTraverser(NoNeedImport) {
      import scala.util.{Try => _, _}
    }
    assertEmpty(result)
  }
  test("`import scala.util._` can be used") {
    val result = WartTestTraverser(NoNeedImport) {
      import scala.util._
      import scala.util.{_}
    }
    assertEmpty(result)
  }
  test("`import scala.util.{ Try => MyTry , _ }` can be used") {
    val result = WartTestTraverser(NoNeedImport) {
      import scala.util.{Try => MyTry, _}
    }
    assertEmpty(result)
  }
}
