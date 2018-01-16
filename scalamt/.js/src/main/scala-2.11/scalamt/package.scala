/**
  * Created by Michael on 21/05/2017.
  * JS
  * Provides help functions for constructing a service.
  */
package object scalamt {

  import upickle.default._

  import scalamt._
  import scalamt.services.{MTServiceHTML, MTServiceRPC, MTServiceWebSocket}

  /**
    * Creates a new HTML Service on the given URL.
    */
  def htmlService(url: String, body: Any = MTServerD, head: Any = MTServerD)(implicit page: MTPage): MTServiceHTML = {
    MTAppInterface.htmlService(url)
  }

  /**
    * Creates a new RPC Service on the given URL.
    */
  def rpcService[U: Writer : Reader, V: Writer : Reader](url: String, sFunction: Any = MTServerD)(implicit page: MTPage): MTServiceRPC[U, V] = {
    MTAppInterface.rpcService[U, V](url)
  }

  /**
    * Creates a new WebSocket Service. (server def of type T => Unit)
    */
  def webSocketService[T: Writer : Reader](sFunction: Any = MTServerD)(implicit page: MTPage): MTServiceWebSocket[T] = {
    MTServiceWebSocket[T]()
  }
}
