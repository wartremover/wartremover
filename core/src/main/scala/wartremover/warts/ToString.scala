package org.wartremover
package warts

import reflect.NameTransformer

object ToString extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val ToString = TermName(NameTransformer.encode("toString"))

    def notOverridden(t: Type): Boolean = {
      val toString = t.member(ToString)
      !isPrimitive(u)(t) && !(t <:< typeOf[String]) &&
        (toString.fullName == "scala.Any.toString" ||
          toString.fullName == "scala.AnyRef.toString" ||
          toString.fullName == "java.lang.Object.toString" ||
          toString.isSynthetic)
    }

    new Traverser {
      override def traverse(tree: Tree) = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          case Apply(Select(lhs, ToString), _) if notOverridden(lhs.tpe) =>
            error(u)(tree.pos, s"${lhs.tpe.baseClasses.head} does not override toString and automatic toString is disabled")

          case _ => super.traverse(tree)

        }
      }
    }
  }
}
