package org.wartremover
package warts

object MapContains extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new Traverser {
      private val mapType = rootMirror.staticClass("scala.collection.Map").toTypeConstructor
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Select(Apply(Select(m, TermName("get")), Seq(arg)), TermName("isDefined" | "isEmpty" | "nonEmpty"))
              if m.tpe.typeConstructor <:< mapType =>
            error(u)(tree.pos, "Maybe you can use `contains`")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
