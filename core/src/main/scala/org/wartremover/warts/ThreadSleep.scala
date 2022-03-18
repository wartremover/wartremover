package org.wartremover
package warts

object ThreadSleep extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val Sleep = TermName("sleep")
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Apply(Select(obj, Sleep), List(arg))
              if (obj.tpe.typeSymbol.fullName == "java.lang.Thread") && (arg.tpe weak_<:< typeOf[Long]) =>
            error(u)(
              tree.pos,
              "don't use Thread.sleep"
            )
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
