package scalamt

/**
  * Created by Michael on 14/11/2016.
  * JS
  * Contains all the client object representations.
  */
//Default client value container - immutable - (val)
sealed trait MTClient[T] {
  protected var _value: T

  final def value: T = _value

  override final def toString: String = _value.toString
}

//Simulate mutable behaviour - (var)
sealed trait MTClientVariable[T] extends MTClient[T] {
  self: MTClientV[T] =>

  final def value_=(newValue: T): Unit =
    self._value = newValue
}

//Client value - (val)
private[scalamt] final case class MTClientN[T](protected var _value: T) extends MTClient[T]

//Client value - (var)
private[scalamt] final case class MTClientV[T](protected var _value: T) extends MTClientVariable[T]

private[scalamt] sealed trait MTFragmentable {
  private[scalamt] val id: MTFragmentID
  type MTFragmentID = Int
}

//Client value - Fragment (not variable)
private[scalamt] final case class MTClientF[T](private[scalamt] val id: Int, protected var _value: T) extends MTClient[T] with MTFragmentable
