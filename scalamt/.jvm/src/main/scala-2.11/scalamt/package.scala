/**
  * Created by Michael on 21/05/2017.
  * JVM
  * Provides help functions for constructing a service.
  */
package object scalamt {

  import scalatags.Text.all._
  import scalamt._
  import upickle.default._
  import scalamt.services.{MTServiceHTML, MTServiceRPC, MTServiceWebSocket}

  /**
    * Creates a new HTML Service on the given URL.
    */
  def htmlService(url: String, body: => MTServer[Seq[Frag]] = MTServerN(Seq()), head: => MTServer[Seq[Frag]] = MTServerN(Seq()))(implicit page: MTPage): MTServiceHTML = {
    MTAppInterface.htmlService(url, body.value, head.value)
  }

  /**
    * Creates a new RPC Service on the given URL. (server variable of type U=>V)
    */
  def rpcService[U: Writer : Reader, V: Writer : Reader](url: String, sFunction: MTServer[U => V])(implicit page: MTPage): MTServiceRPC[U, V] = {
    MTAppInterface.rpcService[U, V](url, sFunction.value)
  }

  /**
    * Creates a new RPC Service on the given URL. (server def of type U => V)
    */
  def rpcService[U: Writer : Reader, V: Writer : Reader](url: String, sFunction: U => MTServer[V])(implicit page: MTPage): MTServiceRPC[U, V] = {
    MTAppInterface.rpcService[U, V](url, sFunction.andThen(_.value))
  }

  /**
    * Creates a new WebSocket Service (server variable of type T => Unit)
    */
  def webSocketService[T: Writer : Reader](sFunction: MTServer[T => Unit]= MTServerN((_:T) => ()))(implicit page:MTPage): MTServiceWebSocket[T] = {
    MTServiceWebSocket[T](sFunction.value)
  }

  /**
    * Creates a new WebSocket Service. (server def of type T => Unit)
    */
  def webSocketService[T: Writer : Reader](sFunction: T => MTServer[Unit])(implicit page:MTPage): MTServiceWebSocket[T] = {
    MTServiceWebSocket[T](sFunction.andThen(_.value))
  }

}
