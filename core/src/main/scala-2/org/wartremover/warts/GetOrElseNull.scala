package org.wartremover
package warts

object GetOrElseNull extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val opt = rootMirror.staticClass("scala.Option").toTypeConstructor
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Apply(
                TypeApply(
                  Select(left, TermName("getOrElse")),
                  _ :: Nil
                ),
                Literal(Constant(null)) :: Nil
              ) if left.tpe.typeConstructor <:< opt =>
            error(u)(tree.pos, "you can use orNull instead of getOrElse(null)")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
