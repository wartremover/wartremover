package org.wartremover
package warts

object Option2Iterable
    extends ExprMatch({ case '{ Option.option2Iterable($x) } =>
      "Implicit conversion from Option to Iterable is disabled - use Option#toList instead"
    })
