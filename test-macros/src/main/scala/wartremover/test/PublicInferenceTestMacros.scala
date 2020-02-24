package wartremover.test

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object PublicInferenceTestMacros {

  def define: Int = macro PublicInferenceTestMacros.define_impl
}

private class PublicInferenceTestMacros(val c: blackbox.Context) {
  import c.universe._

  def define_impl: c.Expr[Int] = {
    val generated = TypeName(c.freshName())
    val tree = q"""{
        class $generated {
          val a_val = 2
          var a_var = 2
          implicit val b_val = 2
          implicit var b_var = 2
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

        new $generated().a_val
        }
        """
    c.Expr[Int](tree)
  }
}

