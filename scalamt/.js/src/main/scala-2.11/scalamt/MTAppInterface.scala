package scalamt

import org.scalajs.dom
import upickle.default._

import scala.util.{Failure, Success, Try}
import scalamt.client.FragmentStore
import scalamt.services.{MTServiceHTML, MTServiceRPC}

/**
  * Created by Michael on 9/11/2016.
  * JS
  * Internal object that acts as a Builder for all the ScalaMT constructions.
  */
object MTAppInterface {

  /**
    * Creates a container which represents a client value of type T.
    */
  def client[T](value: T): MTClient[T] =
    MTClientN(value)

  /**
    * Creates a container which represents a mutable client value of type T.
    */
  def clientV[T](value: T): MTClientVariable[T] =
    MTClientV(value)

  @throws[IDException]
  def clientF(id: Int, value: (dom.Element, dom.Event) => Unit): MTClient[(dom.Element, dom.Event) => Unit] = {
    val fragment = MTClientF(id, value)
    FragmentStore.registerFragment(fragment)
    fragment
  }

  /**
    * Creates a container which represents a dummy server value.
    */
  def server(): MTServer[Nothing] =
    MTServerD()

  /**
    * Creates a container which represents an injectable server value.
    *
    * @throws IDException        If the given ID doesn't exist
    * @throws BadFormatException If string that corresponds to the given ID can't be deserialized into an object of the given type.
    */
  @throws[IDException]
  @throws[BadFormatException]
  def serverI[T: Reader : Writer](id: Int)(implicit page: MTPage): MTServerInjectable[T] = {
    println(page.injectables)
    if (!page.injectables.contains(id))
      throw new IDException("Couldn't create a new injectable server variable because the given ID is not available.")

    Try {
      MTServerI[T](id, read[T](page.injectables(id)))
    } match {
      case Success(result) => result
      case Failure(_) => throw new BadFormatException(s"An injectable server variable with id($id) couldn't be deserialized")
    }

  }

  /**
    * Creates a new HTML Service on the given URL.
    */
  private [scalamt] def htmlService(url: String)(implicit page: MTPage): MTServiceHTML = {
    MTServiceHTML(url)
  }

  /**
    * Creates a new RPC Service on the given URL.
    */
  private [scalamt] def rpcService[U: Writer : Reader, V: Writer : Reader](url: String)(implicit page: MTPage): MTServiceRPC[U, V] = {
    MTServiceRPC[U, V](url)
  }
}
