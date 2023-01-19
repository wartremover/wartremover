package org.wartremover
package warts

object ThreadSleep
    extends ExprMatch { case '{ Thread.sleep($x) } =>
      "don't use Thread.sleep"
    }
