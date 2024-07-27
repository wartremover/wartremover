package org.wartremover

import dotty.tools.dotc.typer.TyperPhase

abstract class WartTraverser { self =>
  private[wartremover] val simpleName: String = {
    Iterator
      .unfold[Class[?], Class[?]](this.getClass)(c => Option(c).map(x => c -> x.getDeclaringClass))
      .toList
      .reverseIterator
      .map(_.getSimpleName)
      .map(s => if (s.endsWith("$")) s.dropRight(1) else s)
      .mkString(".")
  }
  private[wartremover] val fullName: String = this.getClass.getName.dropRight(1)

  def runsAfter: Set[String] = Set(TyperPhase.name)

  def apply(u: WartUniverse): u.Traverser

  def compose(o: WartTraverser): WartTraverser = new WartTraverser {
    override def runsAfter: Set[String] = self.runsAfter ++ o.runsAfter

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
