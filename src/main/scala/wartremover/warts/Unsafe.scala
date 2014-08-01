package org.brianmckenna.wartremover
package warts

object Unsafe extends WartTraverser {
  val safeTraversers = List(
    Any,
    Any2StringAdd,
    AsInstanceOf,
    DefaultArguments,
    EitherProjectionPartial,
    IsInstanceOf,
    NonUnitStatements,
    Null,
    OptionPartial,
    Product,
    Return,
    Serializable,
    TryPartial,
    Var,
    ListOps.Head,
    ListOps.Tail,
    ListOps.Last
  )

  def apply(u: WartUniverse): u.Traverser =
    WartTraverser.sumList(u)(safeTraversers)
}
