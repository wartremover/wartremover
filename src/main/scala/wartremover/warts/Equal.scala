package org.brianmckenna.wartremover
package warts

import reflect.NameTransformer

object Equal extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val EqEqName: TermName = NameTransformer.encode("==")
    val Equals: TermName = NameTransformer.encode("equals")

    // We can only compare types that conform
    def nonConforming(a: Type, b: Type): Boolean = 
      !(a <:< b || b <:< a)

    new Traverser {
      override def traverse(tree: Tree) {
        tree match {

          case Apply(Select(lhs, EqEqName | Equals), List(rhs)) if nonConforming(lhs.tpe, rhs.tpe) =>
            val msg = s"Non-conforming types ${lhs.tpe} and ${rhs.tpe} cannot be compared"
            u.error(tree.pos, msg)

          case _ => super.traverse(tree)

        }          
      }
    }
  }

}
