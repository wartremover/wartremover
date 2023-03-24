package org.wartremover
package warts

object JavaNetURLConstructors extends WartTraverser {

  private[wartremover] def message: String =
    "java.net.URL construcor deprecated https://bugs.openjdk.org/browse/JDK-8295949"

  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Apply(Select(New(clazz), termNames.CONSTRUCTOR), _) if clazz.tpe =:= typeOf[java.net.URL] =>
            error(u)(tree.pos, message)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
