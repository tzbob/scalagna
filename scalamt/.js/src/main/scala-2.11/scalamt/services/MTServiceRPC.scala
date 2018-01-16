package scalamt.services

import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax
import upickle.default._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Michael on 25/02/2017.
  * JS
  * Internal representation of a RPC Service of type [U,V].
  * 1. Client calls the service with an object of type U
  * 2. ScalaMT (Client): serialize this object and makes a POST call to the server on te given URL.
  * 3. ScalaMT (Server): deserialize the string to an object of type U and execute the given SERVER function using this object.
  * 4. ScalaMT (Server): serialize the result of type V and return this to the client.
  * 5. ScalaMT (Client): deserialize the string to an object of type V and execute the given CLIENT function using this object.
  */
case class MTServiceRPC[U: Reader : Writer, V: Reader : Writer] private[scalamt](url: String) extends MTService {
  def apply(ff: U): Future[V] =
    Ajax.post(url, write[U](ff))
      .map((xhr: XMLHttpRequest) => read[V](xhr.responseText))(ExecutionContext.global)

  def call(ff: U, f: V => Unit = _ => ()): Unit =
    Ajax.post(url, write[U](ff))
      .map((xhr: XMLHttpRequest) => read[V](xhr.responseText))(ExecutionContext.global)
      .onSuccess { case v => f(v) }(ExecutionContext.global)
}