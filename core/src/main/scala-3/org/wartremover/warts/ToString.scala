package org.wartremover
package warts

object ToString extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case Apply(s @ Select(lhs, "toString"), Nil)
              if !isPrimitive(lhs.tpe) && !(lhs.tpe <:< TypeRepr.of[String]) &&
                (lhs.tpe.baseClasses.collect {
                  case x if x.declaredMethod("toString").filterNot(_.flags.is(Flags.Synthetic)).nonEmpty => x.fullName
                } == Seq("scala.Any")) =>
            val parent = lhs.tpe.baseClasses.head
            val name = if (parent.flags.is(Flags.Module)) {
              "object " + parent.name.dropRight(1)
            } else {
              "class " + parent.name
            }
            error(selectNamePosition(s), s"${name} does not override toString and automatic toString is disabled")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
