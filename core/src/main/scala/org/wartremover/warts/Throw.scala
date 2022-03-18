package org.wartremover
package warts

object Throw extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val ProductElementName = TermName("productElement")

    // since Scala 2.13 https://github.com/scala/scala/commit/efc48213073ce5e68a7fd7dc9820610eccdeb9f7
    val ProductElementNameName = TermName("productElementName")
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case dd@DefDef(_, ProductElementName | ProductElementNameName, _, _, _, _) if isSynthetic(u)(dd) =>
          case t@u.universe.Throw(Apply(Select(New(exception), _), _)) if (exception.tpe <:< typeOf[scala.MatchError]) && isSynthetic(u)(t) =>
          case u.universe.Throw(_) =>
            error(u)(tree.pos, "throw is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
