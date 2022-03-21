package example

import scala.language.implicitConversions

// https://github.com/wartremover/wartremover/commit/468900530493473f7b022d4da1ad17aa69571b67
final case class B[X](a: X)

@SuppressWarnings(Array("org.wartremover.warts.All"))
class Main {
  final val abc = 4

  def overload(x: Int): String = ""
  def overload(x: String): String = ""

  def nestMethod1 = {
    class NestedClass {

      def nestMethod2 = {
        def x5 = List("x", false) // Any
        def x1 = List(2, true) // AnyVal

        def x3 = Array(1) == Array(2) // ArrayEquals

        // Null, asInstanceOf
        null.asInstanceOf[Long]

        def x2: Int = {
          var i = 0 // Var
          while (i <= 10) { // While
            return i // Return
          }
          99
        }

        // ListUnapply, DefaultArguments
        def x4[B](a: collection.Seq[B] = Nil): Int = a match {
          case _ :: _ :: _ =>
            0
          case _ :: _ =>
            1
          case _ =>
            2
        }

        // ListAppend
        List(3) :+ 4

        "str" + Predef

        Nil.head

        null.isInstanceOf[Int] // IsInstanceOf

        scala.util.Try(9).get

        Option(false).get

        Option(""): Iterable[String]

        scala.io.Source.fromFile("foo.txt") // PlatformDefault

        List(9).size == 2 // SizeIs

        Thread.sleep(33) // ThreadSleep

        // DefaultArguments, ImplicitConversion, ImplicitParameter
        implicit def foo(y: String = "")(implicit p: Long): Option[Int] = None

        println(scala.concurrent.ExecutionContext.global) // GlobalExecutionContext

        println(scala.collection.mutable.ListBuffer[Boolean]()) // MutableDataStructures

        throw new Error("error")

        // EitherProjectionPartial
        @annotation.nowarn
        def either = List(
          Right(3).right.get,
          Left(2).left.get
        )

      }
    }
  }
}
