package scalamt.macros

import scala.annotation.StaticAnnotation

/**
  * Created by Michael on 25/03/2017.
  * Shared - @fragment annotation
  * Use this annotation on a client val declaration to use it as a client fragment.
  * A client fragment can be used in a server expression for describing an event handler for a HTML element.
  */
class fragment extends StaticAnnotation
