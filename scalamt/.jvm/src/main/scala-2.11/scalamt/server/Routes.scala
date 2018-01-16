package scalamt.server

import java.util.concurrent.Executors

import org.http4s.HttpService
import org.http4s.dsl._
import org.http4s.server.staticcontent
import org.http4s.server.staticcontent.ResourceService.Config
import org.http4s.server.websocket.WS
import org.http4s.websocket.WebsocketBits._

import scalamt.MTPage
import scalamt.MTServerSettings._
import scalamt.services.{MTServiceHTML, MTServiceRPC, MTServiceWebSocket}
import scalaz.stream.Exchange

/**
  * Created by Michael on 30/12/2016.
  * JVM
  * This class handles all the routing stuff.
  * HTML service : GET /URL
  * RPC service : POST /URL
  * WebSocket service : GET /ws/pageid/socketid/uuid
  */
private[scalamt] class Routes(pages: List[MTPage]) {
  private val resourcedir = mtResourceDir
  private val static = cachedResource(Config(s"/$resourcedir", s"/$resourcedir"))
  private implicit val scheduledEC = Executors.newScheduledThreadPool(4)
  val service: HttpService = HttpService {

    case r@GET -> Root / `resourcedir` / _ =>
      static(r)

    case r@GET -> Root / "ws" / IntVar(pageid) / IntVar(socketid) / uuid =>
      pages.find(_.pageID == pageid) match {
        case Some(p: MTPage) => p.socketServices.get(socketid) match {
          case Some(socket: MTServiceWebSocket[_]) =>
            def frameToMsg(f: WebSocketFrame) = f match {
              case Text(msg, _) =>
                socket.exec(msg)
                msg
              case _ => s""
            }

            socket.socketTopic.publishOne(s"New user '$uuid' connected")
              .flatMap { _ =>
                val src = socket.socketTopic.subscribe.map(Text(_))
                val snk = socket.socketTopic.publish.contramap(frameToMsg)

                WS(Exchange(src, snk))
              }
          case _ => NotFound()
        }
        case _ => NotFound()
      }

    case req@GET -> url =>
      pages.find(_.htmlServices.contains(url.toString)) match {
        case Some(p: MTPage) => p.htmlServices.get(url.toString) match {
          case Some(html: MTServiceHTML) => html.response
          case _ => NotFound()
        }
        case None => NotFound()
      }

    case req@POST -> url =>
      pages.find(_.rpcServices.contains(url.toString)) match {
        case Some(p: MTPage) => p.rpcServices.get(url.toString) match {
          case Some(rpc: MTServiceRPC[_, _]) => rpc.exec(req)
          case _ => NotFound()
        }
        case None => NotFound()
      }

    case _ =>
      NotFound()
  }

  private def cachedResource(config: Config): HttpService = {
    val cachedConfig = config.copy(cacheStrategy = staticcontent.MemoryCache())
    staticcontent.resourceService(cachedConfig)
  }
}