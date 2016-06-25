package org.wartremover
package warts

object JavaConversions extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val javaConversions = rootMirror.staticModule("scala.collection.JavaConversions")

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Select(tpt, _) if tpt.tpe.contains(javaConversions) => {
            u.error(tree.pos, "scala.collection.JavaConversions is disabled - use scala.collection.JavaConverters instead")
            super.traverse(tree)
          }
          case _ => super.traverse(tree)
        }
      }
    }
  }
}
