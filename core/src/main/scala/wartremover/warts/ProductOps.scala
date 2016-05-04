package org.brianmckenna.wartremover
package warts

object ProductOps extends WartTraverser {

  class Op(name: String, error: String) extends WartTraverser {
    override lazy val className = "org.brianmckenna.wartremover.warts.ProductOps"

    def apply(u: WartUniverse): u.Traverser = {
      import u.universe._

      val ProductSymbol = rootMirror.staticClass("scala.Product")
      val Name: TermName = name
      new u.Traverser {
        override def traverse(tree: Tree): Unit = {
          tree match {
            // Ignore trees marked by SuppressWarnings
            case t if hasWartAnnotation(u)(t) =>
            case Select(left, Name) if left.tpe.baseType(ProductSymbol) != NoType ⇒
              u.error(tree.pos, error)
            // TODO: This ignores a lot
            case LabelDef(_, _, rhs) if isSynthetic(u)(tree) ⇒
            case _ ⇒
              super.traverse(tree)
          }
        }
      }
    }
  }

  def apply(u: WartUniverse): u.Traverser =
    WartTraverser.sumList(u)(List(
      new Op("canEqual", "Product#canEqual is disabled - consider using type classes for type-safe equality instead"),
      new Op("productArity", "Product#productArity is disabled - it is not a very helpful abstraction"),
      new Op("productElement", "Product#productElement is disabled - it is unsafe and its return type of Any is not a very helpful abstraction"),
      new Op("productIterator", "Product#produceIterator is disabled - Iterator[Any] is not a very helpful abstraction"),
      new Op("productPrefix", "Product#productPrefix is disabled - it is not a very helpful abstraction")
    ))

}
