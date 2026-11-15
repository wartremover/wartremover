package org.wartremover
package warts

/**
 * [[https://github.com/scala/scala3/pull/26794]]
 */
object CatchThrowable extends WartTraverser {
  private def message: String = "Catching Throwable can lead to unexpected behavior"

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case t: Try =>
            t.catches.iterator.map(_.pat).foreach {
              case p: Typed if p.tpt.tpe.dealias.typeSymbol.fullName == "java.lang.Throwable" =>
                error(u)(p.pos, message)
              case Bind(_, p: Typed) if p.tpt.tpe.dealias.typeSymbol.fullName == "java.lang.Throwable" =>
                error(u)(p.pos, message)
              case _ =>
            }
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
