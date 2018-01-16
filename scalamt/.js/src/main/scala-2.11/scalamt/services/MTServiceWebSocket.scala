package scalamt.services

import org.scalajs.dom
import org.scalajs.dom._
import upickle.default._

import scala.util.Try
import scalamt.MTPage

/**
  * Created by Michael on 2/06/2017.
  * JS
  * Internal representation of a WebSocket Service.
  * Important: currently there is no support for the re-opening of a WebSocket once it has been closed.
  */
case class MTServiceWebSocket[T: Reader : Writer] private[scalamt](implicit val page: MTPage) extends MTService {
  private val wsProtocol: String = if (dom.document.location.protocol == "https:") "wss" else "ws"
  private val socketID = MTServiceWebSocket.getID
  val url: String = s"$wsProtocol://${dom.document.location.host}/ws/${page.pageID}/$socketID/${page.uuid}"
  private var webSocket: WebSocket = new WebSocket(url)

  def send(message: T): Unit =
    webSocket.send(write[T](message))

  def onmessage(f: T => Unit): Unit = {
    webSocket.onmessage = (event: MessageEvent) => Try {
      f(read[T](event.data.toString))
    }
  }

  def onopen(f: Event => Unit): Unit = {
    webSocket.onopen = f
  }

  def onerror(f: ErrorEvent => Unit): Unit = {
    webSocket.onerror = f
  }
}

object MTServiceWebSocket {
  private var _id = 0

  private def getID: Int = {
    _id = _id + 1
    _id
  }
}