package org.brianmckenna.wartremover
package warts

object Unsafe extends WartTraverser {
  val safeTraversers = List(
    Any2StringAdd,
    NonUnitStatements,
    Null,
    OptionPartial,
    Var
  )

  def apply(u: WartUniverse): u.Traverser =
    WartTraverser.sumList(u)(safeTraversers)
}
