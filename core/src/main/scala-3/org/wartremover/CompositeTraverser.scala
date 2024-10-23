package org.wartremover

abstract class CompositeTraverser(val traversers: List[WartTraverser]) extends WartTraverser {
  final def apply(u: WartUniverse): u.Traverser = {
    if (traversers.nonEmpty) {
      WartTraverser.sumList(u)(traversers)
    } else {
      new u.Traverser(this) {
        import q.reflect.*
        override def traverseTree(tree: Tree)(owner: Symbol): Unit = ()
      }
    }
  }
}
