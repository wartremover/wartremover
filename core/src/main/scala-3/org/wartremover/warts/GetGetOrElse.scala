package org.wartremover
package warts

object GetGetOrElse
    extends ExprMatch { case '{ ($x: collection.Map[t1, t2]).get($k).getOrElse($v) } =>
      "you can use Map#getOrElse(key, default) instead of get(key).getOrElse(default)"
    }
