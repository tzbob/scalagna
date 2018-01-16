package scalamt.services

import org.http4s.Response
import org.http4s.dsl._
import upickle.default._

import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scala.util.Try
import scalamt.MTPage
import scalamt.MTServerSettings._
import scalaz.concurrent.Task
import scalaz.stream.async.mutable.Topic
import scalaz.stream.async.topic

/**
  * Created by Michael on 2/06/2017.
  * JVM
  * Internal representation of a WebSocket Service.
  * Important: currently there is no support for the re-opening of a WebSocket once it has been closed.
  */
case class MTServiceWebSocket[T: Reader : Writer] private[scalamt](sFunction: T => Unit)(implicit page: MTPage) extends MTService {
  private val wsProtocol: String = if (getProtocol == "https:") "wss" else "ws"
  private[scalamt] val socketID = MTServiceWebSocket.getID
  val url: String = s"$wsProtocol://$mtHost/ws/${page.pageID}$socketID"
  private[scalamt] val socketTopic: Topic[String] = topic[String]()

  //Currently the framework doesn't support https
  private def getProtocol: String = "http;"

  private[scalamt] def exec(msg: String): Task[Response] = Try {
    Ok(sFunction(read[T](msg)))
  }.getOrElse(BadRequest())

  def send(message: Any): Unit = macro MTServiceWebSocket.send_

  def onmessage(f: Any): Unit = macro MTServiceWebSocket.onmessage_

  def onopen(f: Any): Unit = macro MTServiceWebSocket.onopen_

  def onerror(f: Any): Unit = macro MTServiceWebSocket.onerror_
}

object MTServiceWebSocket {
  private var _id = 0

  private def getID: Int = {
    _id = _id + 1
    _id
  }

  def send_(c: whitebox.Context)(message: c.Tree): c.Expr[Nothing] =
    c.abort(c.enclosingPosition, "You can only send data trough a WebSocket (_.send) in a client expression.")

  def onmessage_(c: whitebox.Context)(f: c.Tree): c.Expr[Nothing] =
    c.abort(c.enclosingPosition, "You can only specify the \"onmessage\" variable in a client expression.")

  def onopen_(c: whitebox.Context)(f: c.Tree): c.Expr[Nothing] =
    c.abort(c.enclosingPosition, "You can only specify the \"onopen\" variable in a client expression.")

  def onerror_(c: whitebox.Context)(f: c.Tree): c.Expr[Nothing] =
    c.abort(c.enclosingPosition, "You can only specify the \"onerror\" variable in a client expression.")
}