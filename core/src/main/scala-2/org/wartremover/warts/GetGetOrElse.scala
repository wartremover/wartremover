package org.wartremover
package warts

object GetGetOrElse extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val map = rootMirror.staticClass("scala.collection.Map").toTypeConstructor
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Apply(
                TypeApply(
                  Select(
                    Apply(Select(left, TermName("get")), _ :: Nil),
                    TermName("getOrElse")
                  ),
                  _ :: Nil
                ),
                _ :: Nil
              ) if left.tpe.typeConstructor <:< map =>
            error(u)(tree.pos, "you can use Map#getOrElse(key, default) instead of get(key).getOrElse(default)")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
