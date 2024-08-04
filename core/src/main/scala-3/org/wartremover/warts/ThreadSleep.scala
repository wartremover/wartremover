package org.wartremover
package warts

object ThreadSleep
    extends ExprMatch({ case '{ Thread.sleep($x: Long) } =>
      "don't use Thread.sleep"
    })
