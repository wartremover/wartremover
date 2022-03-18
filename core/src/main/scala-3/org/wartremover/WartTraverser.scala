package org.wartremover

abstract class WartTraverser { self =>
  private[wartremover] val simpleName: String = this.getClass.getSimpleName.dropRight(1)
  private[wartremover] val fullName: String = this.getClass.getName.dropRight(1)

  def apply(u: WartUniverse): u.Traverser

  def compose(o: WartTraverser): WartTraverser = new WartTraverser {
    override def apply(u: WartUniverse): u.Traverser = {
      import u.quotes.reflect.*
      new u.Traverser(this) {
        override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
          self.apply(u).traverseTree(tree)(owner)
          o.apply(u).traverseTree(tree)(owner)
        }
      }
    }
  }
}

object WartTraverser {
  def sumList(u: WartUniverse)(l: List[WartTraverser]): u.Traverser =
    l.reduceRight(_ compose _)(u)
}
