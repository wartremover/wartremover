package org.brianmckenna.wartremover
package warts

object Unsafe extends WartTraverser {
  val safeTraversers = List(
    Any,
    Any2StringAdd,
    AsInstanceOf,
    EitherProjectionPartial,
    IsInstanceOf,
    NonUnitStatements,
    Null,
    OptionPartial,
    Product,
    Return,
    Serializable,
    Var
  )

  def apply(u: WartUniverse): u.Traverser =
    WartTraverser.sumList(u)(safeTraversers.reverse)
}
