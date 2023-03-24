package org.wartremover
package warts

object JavaNetURLConstructors extends WartTraverser {

  private[wartremover] def message: String =
    "java.net.URL construcor deprecated https://bugs.openjdk.org/browse/JDK-8295949"

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case New(t) if t.tpe =:= TypeRepr.of[java.net.URL] =>
            error(t.pos, message)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
