package wartremover.test

import scala.quoted.Expr
import scala.quoted.Quotes

object PublicInferenceTestMacros {

  inline def define: Int = ${ define_impl }

  private[this] def define_impl(using Quotes): Expr[Int] = '{
    class generated {
      val a_val = 2
      var a_var = 2
      implicit val b_val: Int = 2
      implicit var b_var: Int = 2
      val c_var: Int = 3
      var c_val: Int = 3

      def a() = {
        val a_val = 2
        var a_var = 2
        println {
          val k_val = 2
          var k_var = 2
          k_val + k_var
        }
      }
      def b: Int = {
        a_val + a_var
      }
    }

    new generated().a_val
  }
}
