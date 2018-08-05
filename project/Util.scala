object Util {

  private[this] def getSuperClasses(clazz: Class[_]): List[Class[_]] = {
    @scala.annotation.tailrec
    def loop(o: Class[_], result: List[Class[_]]): List[Class[_]] = {
      val superClass = o.getSuperclass
      if (superClass == null) {
        result
      } else {
        loop(superClass, superClass :: result)
      }
    }

    loop(clazz, List(clazz))
  }

  private[this] def getAllClassAndTrait(clazz: Class[_]): List[Class[_]] = {
    def loop(c: Class[_], result: List[Class[_]]): List[Class[_]] = {
      val interfaces = c.getInterfaces.toList

      if (interfaces.size == 0) {
        result
      } else {
        interfaces.flatMap { i => loop(i, i :: result) }
      }
    }

    ({
      for {
        c <- getSuperClasses(clazz)
        result <- loop(clazz, List(c))
      } yield result
    }.toSet - clazz).toList
  }

  def isWartClass(clazz: Class[_]): Boolean = {
    val parents = getAllClassAndTrait(clazz)
    parents.exists(_.toString == "interface org.wartremover.WartTraverser")
  }

}
