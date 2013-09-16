package org.brianmckenna.wartremover
package warts

object Unsafe extends WartTraverser {
  val safeTraversers = List(NonUnitStatements, Var, Null, Any2StringAdd)

  def apply(u: WartUniverse): u.Traverser =
    WartTraverser.sumList(u)(safeTraversers)
}
