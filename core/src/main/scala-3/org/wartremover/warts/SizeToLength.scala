package org.wartremover
package warts

object SizeToLength extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "size") =>
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            def err(): Unit = error(t.pos, "Maybe you should use `length` instead of `size`")

            t.asExpr match {
              case '{ Predef.genericArrayOps[t]($x1).size } =>
                err()
              case '{ Predef.booleanArrayOps($x1).size } =>
                err()
              case '{ Predef.byteArrayOps($x1).size } =>
                err()
              case '{ Predef.charArrayOps($x1).size } =>
                err()
              case '{ Predef.doubleArrayOps($x1).size } =>
                err()
              case '{ Predef.floatArrayOps($x1).size } =>
                err()
              case '{ Predef.intArrayOps($x1).size } =>
                err()
              case '{ Predef.longArrayOps($x1).size } =>
                err()
              case '{
                    type t <: AnyRef
                    Predef.refArrayOps[`t`]($x1).size
                  } =>
                err()
              case '{ Predef.shortArrayOps($x1).size } =>
                err()
              case '{ Predef.unitArrayOps($x1).size } =>
                err()
              case '{ Predef.augmentString($x1).size } =>
                err()
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
