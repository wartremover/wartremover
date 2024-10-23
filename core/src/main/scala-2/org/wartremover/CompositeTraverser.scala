package org.wartremover

abstract class CompositeTraverser(val traversers: List[WartTraverser]) extends WartTraverser {
  final def apply(u: WartUniverse): u.Traverser = {
    if (traversers.nonEmpty) {
      WartTraverser.sumList(u)(traversers)
    } else {
      import u.universe._
      new u.Traverser {
        override def traverse(tree: Tree): Unit = ()
      }
    }
  }
}
