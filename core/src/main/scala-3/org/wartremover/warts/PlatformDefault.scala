package org.wartremover
package warts

object PlatformDefault extends WartTraverser {

  private[wartremover] def getByte: String =
    "please specify charset parameter. don't use platform's default charset https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#getBytes--"

  private[wartremover] def upperLowerCase: String =
    "please specify locale parameter. don't use platform's default locale https://docs.oracle.com/javase/8/docs/api/java/lang/String.html"

  private[wartremover] def newString: String =
    "please specify charset parameter. don't use platform's default charset https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#String-byte:A-"

  private[wartremover] def fallbackSystemCodec: String =
    "don't use scala.io.Codec.fallbackSystemCodec. don't use platform's default charset"

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: String).getBytes } =>
                error(tree.pos, getByte)
              case '{ ($x1: String).toLowerCase } | '{ ($x2: String).toUpperCase } =>
                error(tree.pos, upperLowerCase)
              case '{ new String(($x: Array[Byte])) } =>
                error(tree.pos, newString)
              case '{ scala.io.Codec.fallbackSystemCodec } =>
                error(tree.pos, fallbackSystemCodec)
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
