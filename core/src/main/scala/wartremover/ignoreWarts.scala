package org.brianmckenna.wartremover

import scala.annotation.StaticAnnotation

class ignoreWarts(warts : String*) extends StaticAnnotation
