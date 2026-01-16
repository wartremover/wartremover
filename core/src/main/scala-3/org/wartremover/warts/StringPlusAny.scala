package org.wartremover
package warts

import scala.annotation.nowarn
import scala.quoted.Expr

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

      @nowarn("msg=any2stringadd")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case Apply(Select(lhs, "+"), List(rhs))
              if lhs.tpe <:< TypeRepr.of[String] && !(rhs.tpe <:< TypeRepr.of[String]) =>
            error(tree.pos, "Implicit conversion to string is disabled")
          case Apply(Select(lhs, "+"), rhs :: Nil)
              if primitiveTypes.exists(lhs.tpe <:< _) && (rhs.tpe <:< TypeRepr.of[String]) =>
            error(tree.pos, "Implicit conversion to string is disabled")
          case t if t.isExpr =>
            t.asExpr match {
              case '{ new Predef.any2stringadd($x) } =>
                error(tree.pos, "Implicit conversion to string is disabled")
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
