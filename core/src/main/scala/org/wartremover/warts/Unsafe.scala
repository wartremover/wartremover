package org.wartremover
package warts

object Unsafe extends WartTraverser {
  val safeTraversers: List[WartTraverser] = List(
    Any,
    AsInstanceOf,
    DefaultArguments,
    EitherProjectionPartial,
    IsInstanceOf,
    IterableOps,
    NonUnitStatements,
    Null,
    OptionPartial,
    Product,
    Return,
    Serializable,
    StringPlusAny,
    Throw,
    TryPartial,
    Var
  )

  def apply(u: WartUniverse): u.Traverser =
    WartTraverser.sumList(u)(safeTraversers)
}
