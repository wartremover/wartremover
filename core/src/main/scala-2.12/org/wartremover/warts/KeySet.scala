package org.wartremover
package warts

object KeySet extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val mapSymbol = rootMirror.staticClass("scala.collection.Map")
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Select(
                Apply(
                  Apply(
                    TypeApply(Select(obj, TermName("map")), _ :: _ :: Nil),
                    Function(
                      List(ValDef(_, t1, TypeTree(), EmptyTree)),
                      Select(Ident(t2), TermName("_1"))
                    ) :: Nil,
                  ),
                  _ :: Nil
                ),
                TermName("toSet")
              ) if t1 == t2 && obj.tpe.baseType(mapSymbol) != NoType =>
            error(u)(tree.pos, "you can use keySet")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
