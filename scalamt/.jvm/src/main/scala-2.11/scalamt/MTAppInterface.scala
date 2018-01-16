package scalamt

import scalamt.services.{MTServiceHTML, MTServiceRPC}
import upickle.default._

import scalatags.Text.all._

/**
  * Created by Michael on 9/11/2016.
  * JVM
  * Internal object that acts as a Builder for all the ScalaMT constructions.
  */
object MTAppInterface {

  /**
    * Creates a container which represents a dummy client value.
    */
  def client(): MTClient[Nothing] = MTClientD()

  /**
    * Creates a container which represents a client fragment.
    */
  def clientF(id: Int): MTClientFragment = MTClientF(id)

  /**
    * Creates a container which represents a server value of type T.
    */
  def server[T](value: T): MTServer[T] =
    MTServerN(value)

  /**
    * Creates a container which represents a mutable server value of type T.
    */
  def serverV[T](value: T): MTServerVariable[T] =
    MTServerNV(value)

  /**
    * Creates a container which represents an injectable server value.
    *
    * @throws IDException If the given ID already exists
    */
  @throws[IDException]
  def serverI[T: Reader : Writer](id: Int, value: T)(implicit page: MTPage): MTServer[T] = {
    val v = MTServerI[T](id, value)
    addInjectable(v)
    v
  }

  /**
    * Creates a container which represents an mutable injectable server value.
    *
    * @throws IDException If the given ID already exists
    */
  @throws[IDException]
  def serverIV[T: Reader : Writer](id: Int, value: T)(implicit page: MTPage): MTServerVariable[T] = {
    val v = MTServerIV[T](id, value)
    addInjectable(v)
    v
  }

  /**
    * Register the given injectable server variable in the page.
    *
    * @throws IDException If the given ID already exists
    */
  @throws[IDException]
  private def addInjectable(injectable: MTInjectable)(implicit page: MTPage): Unit = {
    if (page.injectables.exists(_.id == injectable.id))
      throw new IDException("An injectable server variable with the same ID already exists.")
    page.injectables += injectable
  }

  /**
    * Creates a new HTML Service on the given URL.
    */
  private[scalamt] def htmlService(url: String, body: => Seq[Frag] = Seq(), head: => Seq[Frag] = Seq())(implicit page: MTPage): MTServiceHTML = {
    MTServiceHTML(url)(body, head)(page)
  }

  /**
    * Creates a new RPC Service on the given URL.
    */
  private[scalamt] def rpcService[U: Writer : Reader, V: Writer : Reader](url: String, sFunction: U => V)(implicit page: MTPage): MTServiceRPC[U, V] = {
    MTServiceRPC[U, V](url, sFunction)
  }
}
