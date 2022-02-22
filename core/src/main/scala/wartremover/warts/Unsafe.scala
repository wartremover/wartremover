package org.wartremover
package warts

object Unsafe extends WartTraverser with Container {
  val items = List(
    Any,
    AsInstanceOf,
    DefaultArguments,
    EitherProjectionPartial,
    IsInstanceOf,
    TraversableOps,
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
    WartTraverser.sumList(u)(items)
}
