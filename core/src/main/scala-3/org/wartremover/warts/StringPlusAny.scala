package org.wartremover
package warts

object StringPlusAny extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      val primitiveTypes: Seq[TypeRepr] = Seq(
        TypeRepr.of[Byte],
        TypeRepr.of[Short],
        TypeRepr.of[Char],
        TypeRepr.of[Int],
        TypeRepr.of[Long],
        TypeRepr.of[Float],
        TypeRepr.of[Double],
      )

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case Apply(s @ Select(lhs, "+"), List(rhs))
              if lhs.tpe <:< TypeRepr.of[String] && !(rhs.tpe <:< TypeRepr.of[String]) =>
            error(selectNamePosition(s), "Implicit conversion to string is disabled")
          case Apply(s @ Select(lhs, "+"), rhs :: Nil)
              if primitiveTypes.exists(lhs.tpe <:< _) && (rhs.tpe <:< TypeRepr.of[String]) =>
            error(selectNamePosition(s), "Implicit conversion to string is disabled")
          case _ =>
            // https://github.com/scala/scala3/commit/1903b4ad8cf709cb729b8967e9708927ffa6688a
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
