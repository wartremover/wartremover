package example

import Main.*

class Main {
  private def x(y: Int) = {
    y match {
      case 1 =>
        List(Some(B))
      case 2 =>
        List(None)
      case _ =>
        List(Some(C))
    }
  }.map(a => a)
}

object Main {
  private sealed trait A extends Product with Serializable
  private case object B extends A
  private case object C extends A
}

enum ExampleEnum[X] {
  case A1 extends ExampleEnum[Int]
  case A2 extends ExampleEnum[String]
}
