package scalamt

import scala.reflect.macros.whitebox
import scalatags.generic.{Attr, AttrValue}
import scalatags.text.Builder

/**
  * Created by Michael on 14/11/2016.
  * JVM
  * Contains all the client object representations.
  */
//Default client value container - immutable - (val)
sealed trait MTClient[+T] {
  import scala.language.experimental.macros

  //The .value method can't be used in a server context
  def value: Nothing = macro MTClient.value
  //The value can't be modified in a server context
  def value_=(value: Any): Nothing = macro MTClient.value_
}

private object MTClient {
  def value(c: whitebox.Context): c.Expr[Nothing] = {
    c.abort(c.enclosingPosition, "It is not allowed to retrieve a client value (_.value) in a server or shared section.")
  }

  def value_(c: whitebox.Context)(value: c.Tree): c.Expr[Nothing] = {
    c.abort(c.enclosingPosition, "It is not allowed to change a client value (_.value = ...) in a server or shared section.")
  }
}

sealed trait MTClientFragment extends MTClient[Nothing]{
  protected val id: MTFragmentID
  type MTFragmentID = Int

  protected val fragmentString: String = s"FStore.call(this, event, $id)"
}

private object MTClientFragment {

  implicit object FragToAttrValue extends AttrValue[Builder, MTClientFragment] {
    override def apply(t: Builder, a: Attr, v: MTClientFragment): Unit =
      t.setAttr(a.name, Builder.GenericAttrValueSource(v.fragmentString))
  }

}

private[scalamt] final case class MTClientD() extends MTClient[Nothing]{

}

private[scalamt] final case class MTClientF(protected val id: Int) extends MTClientFragment

//Tag --> generate id for injecitions
//--> implicit decoder en ecoder voor serverInjectable
//Client eventahndelers @client getelementbyid ...
//Server[Tag] --> Server[Nothing]
//cleint Server[+A] zonder value
//JVM SErver[A]] met value
//Client omgekeerd
/*private object MTClientF {

  implicit object FragToAttrValue extends AttrValue[Builder, MTClientF] {
    override def apply(t: Builder, a: Attr, v: MTClientF): Unit =
      t.setAttr(a.name, Builder.GenericAttrValueSource(v.fragmentString))
  }

}*/

/*object MTObjectDummy {
  def value = null
}*/

/*
class ServerInjectable[T: Reader : Writer] private(_v: T) extends MTServer[T](_v) {
  private val id:Int = 0
  private var str = ""

  def this(id: Int, _v: T) = {
    this(_v)
    update()
  }

  override def value_=(f: (T) => T): Unit = {
    super.value_=(f)
    update()
  }

  override def value_=(newValue: T): Unit = {
    super.value_=(newValue)
    update()
  }

  private def update(): Unit = {
    str = upickle.default.write[(Int, T)]((id, super.value))
  }

  private[scalamt] def write: String = str
}
*/
