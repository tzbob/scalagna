package scalamt

import scala.reflect.macros.whitebox
import upickle.default._
import scalamt.macros.MacroLogger._

/**
  * Created by Michael on 18/04/2017.
  * JS - MTServer
  */
//Default server value container - immutable - (val)
sealed trait MTServer[+T] {

  import scala.language.experimental.macros

  //The .value method can't be used in a client context
  final def value: Nothing = macro MTServer.impl1
  //The value can't be modified in a client context
  final def value_= (value:Any): Nothing = macro MTServer.impl2
}

private object MTServer {
  def impl1(c: whitebox.Context): c.Expr[Nothing] = {
    abort(c, "It is not allowed to retrieve a server value (_.value) in a client or shared section. \n For an injectable server value use _.inject instead.")
  }

  def impl2(c: whitebox.Context)(value: c.Tree): c.Expr[Nothing] = {
    abort(c, "It is not allowed to change a server value (_.value = ...)  in a client or shared section.")
  }
}

//Simulate injectable behaviour from a value which lives on the server.
sealed trait MTServerInjectable[T] extends MTServer[T] {
  self: MTServerI[T] =>

  protected var _value: T

  final def inject: T = _value

  override final def toString: String = _value.toString
}

//Server value - Injectable
private[scalamt] final case class MTServerI[T: Reader : Writer](private[scalamt] val id: Int, protected var _value: T) extends MTServerInjectable[T]

//Server value - Dummy
private[scalamt] final case class MTServerD() extends MTServer[Nothing]
