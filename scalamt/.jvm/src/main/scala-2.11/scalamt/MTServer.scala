package scalamt

import upickle.default._

/**
  * Created by Michael on 17/04/2017.
  * JVM - MTServer
  */
//Default server value container - immutable - (val)
sealed trait MTServer[+T] {
  def value: T

  override final def toString: String = value.toString
}

//Simulate mutable behaviour - (var)
sealed trait MTServerVariable[T] extends MTServer[T] {
  self: MTServer[T] =>

  var _value: T

  def value = _value
  def value_=(newValue: T): Unit =
    self._value = newValue
}

private[scalamt] sealed trait MTInjectable {
  self: MTServer[_] =>

  private[scalamt] val id: MTInjectionID
  type MTInjectionID = Int

  private[scalamt] def write: (String, String)
}

//Server value
private[scalamt] final case class MTServerN[T](value: T) extends MTServer[T]

//Server value - Variable
private[scalamt] final case class MTServerNV[T](var _value: T)
    extends MTServerVariable[T]

//Server value - Injectable
private[scalamt] final case class MTServerI[T: Reader: Writer](
    private[scalamt] val id: Int,
    value: T)
    extends MTServer[T]
    with MTInjectable {
  override def write: (String, String) =
    (upickle.default.write[Int](id), upickle.default.write[T](value))
}

//Server value - Injectable - Variable
private[scalamt] final case class MTServerIV[T: Reader: Writer](
    private[scalamt] val id: Int,
    var _value: T)
    extends MTServerVariable[T]
    with MTInjectable {
  override def write: (String, String) =
    (upickle.default.write[Int](id), upickle.default.write[T](value))
}
