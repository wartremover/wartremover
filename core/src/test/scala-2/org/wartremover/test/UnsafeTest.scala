package org.wartremover
package test

import org.wartremover.warts.Unsafe
import org.scalatest.funsuite.AnyFunSuite

// TODO Scala 3 ?
class UnsafeTest extends AnyFunSuite {
  test(
    "can't use `null`, `var`, non-unit statements, Option#get, LeftProjection#get, RightProjection#get, or any2stringadd"
  ) {
    val result = WartTestTraverser(Unsafe) {
      val x = List(1, true, "three")
      var u = {} + "Hello!"
      Some(10).get
      println(Left(42).left.get)
      println(Left(42).right.get)
      println(Right(42).left.get)
      println(Right(42).right.get)
      println(null)
    }
    assertResult(
      Set(
        "[wartremover:Any] Inferred type containing Any: Any",
        "[wartremover:EitherProjectionPartial] LeftProjection#get is disabled - use LeftProjection#toOption instead",
        "[wartremover:Any] Inferred type containing Any: List[Any]",
        "[wartremover:EitherProjectionPartial] RightProjection#get is disabled - use RightProjection#toOption instead",
        "[wartremover:NonUnitStatements] Statements must return Unit",
        "[wartremover:Null] null is disabled",
        "[wartremover:OptionPartial] Option#get is disabled - use Option#fold instead",
        "[wartremover:StringPlusAny] Implicit conversion to string is disabled",
        "[wartremover:Var] var is disabled"
      ),
      "result.errors"
    )(result.errors.toSet)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
