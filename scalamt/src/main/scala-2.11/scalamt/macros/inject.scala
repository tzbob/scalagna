package scalamt.macros

import scala.annotation.StaticAnnotation

/**
  * Created by Michael on 28/01/2017.
  * Shared - @inject annotation
  * Put this annotation on a server variable declaration to make it injectable.
  * An injectable server variable can be used in a client expression.
  */
class inject extends StaticAnnotation

