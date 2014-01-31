package org.brianmckenna.wartremover
package warts

import reflect.NameTransformer

object Equal extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val EqEqName: TermName = NameTransformer.encode("==")

    // We can only compare types that conform
    def bogus(a: Type, b: Type): Boolean = 
      !(a <:< b || b <:< a)

    new Traverser {
      override def traverse(tree: Tree) {
        tree match {

          case Apply(Select(lhs, EqEqName), List(rhs)) if bogus(lhs.tpe, rhs.tpe) =>
            val msg = s"Non-conforming types ${lhs.tpe} and ${rhs.tpe} cannot be compared"
            u.error(tree.pos, msg)

          case _ => super.traverse(tree)

        }          
      }
    }
  }

}
