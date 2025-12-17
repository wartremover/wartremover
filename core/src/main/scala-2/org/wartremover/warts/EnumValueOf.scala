package org.wartremover
package warts

object EnumValueOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case t @ Apply(Select(obj, TermName("valueOf")), arg :: Nil)
              if (obj.tpe.typeSymbol.companion == t.tpe.typeSymbol) && (
                arg.tpe <:< typeOf[String]
              ) && t.tpe.baseClasses.exists(_.fullName == "java.lang.Enum") =>
            error(u)(tree.pos, "Enum.valueOf is disabled")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
