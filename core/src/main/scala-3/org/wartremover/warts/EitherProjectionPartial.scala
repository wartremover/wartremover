package org.wartremover
package warts

import scala.annotation.nowarn

@nowarn("msg=right-biased")
@nowarn("msg=Either.toOption.get")
@nowarn("msg=Either.swap.getOrElse")
object EitherProjectionPartial
    extends ExprMatch({
      case '{ (${ a }: Either.RightProjection[left, right]).get } =>
        "RightProjection#get is disabled - use RightProjection#toOption instead"
      case '{ (${ a }: Either.LeftProjection[left, right]).get } =>
        "LeftProjection#get is disabled - use LeftProjection#toOption instead"
    })
