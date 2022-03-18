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
    import u.universe._
    val FallbackSystemCodec = TermName("fallbackSystemCodec")
    val GetBytes = TermName("getBytes")
    val ToLowerCase = TermName("toLowerCase")
    val ToUpperCase = TermName("toUpperCase")
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          // Ignore trees marked by SuppressWarnings
          case Apply(Select(obj, GetBytes), Nil) if obj.tpe <:< typeOf[String] =>
            error(u)(tree.pos, getByte)
          case Apply(Select(obj, ToLowerCase | ToUpperCase), Nil) if obj.tpe <:< typeOf[String] =>
            error(u)(tree.pos, upperLowerCase)
          case Apply(Select(New(clazz), termNames.CONSTRUCTOR), List(arg))
              if clazz.tpe =:= typeOf[String] && arg.tpe =:= typeOf[Array[Byte]] =>
            error(u)(tree.pos, newString)
          case Select(obj, FallbackSystemCodec) if obj.tpe =:= typeOf[scala.io.Codec.type] =>
            // `fallbackSystemCodec` depends on `java.nio.charset.Charset.defaultCharset`
            // https://github.com/scala/scala/blob/v2.13.6/src/library/scala/io/Codec.scala#L80
            error(u)(tree.pos, fallbackSystemCodec)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
