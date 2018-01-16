package scalamt

import scala.collection.mutable.{ListBuffer => List2, Map => Map2}
import scalamt.MTPage.MTPageID
import scalamt.services.{MTService, MTServiceHTML, MTServiceRPC, MTServiceWebSocket}

/**
  * Created by Michael on 6/04/2017.
  * A page represents a part of the full application.
  * For each HTML service created in a page, the server includes the same script.
  * This script executes the client version of this page using the execute method.
  * Application logic for a Page is added into the implementation of the execute method.
  * The services in the result of the execute method will be activated.
  */
trait MTPage {
  final implicit val page: MTPage = this
  final private[scalamt] val pageID: MTPageID = MTPage.getPageID

  private[scalamt] val injectables: List2[MTInjectable] = List2.empty
  private[scalamt] val htmlServices: Map2[String, MTServiceHTML] = Map2.empty
  private[scalamt] val rpcServices: Map2[String, MTServiceRPC[_, _]] = Map2.empty
  private[scalamt] val socketServices: Map2[Int, MTServiceWebSocket[_]] = Map2.empty

  private[scalamt] def init(): Unit = {
    execute().foreach({
      case html: MTServiceHTML => htmlServices += (html.url -> html)
      case rpc: MTServiceRPC[_, _] => rpcServices += (rpc.url -> rpc)
      case websocket: MTServiceWebSocket[_] => socketServices += (websocket.socketID -> websocket)
    })
  }

  private[scalamt] def execute(): List[MTService]
}

private object MTPage {
  final type MTPageID = Int

  private[this] var pageIDGenerator: Int = 0

  private def getPageID: Int = {
    pageIDGenerator += 1
    pageIDGenerator
  }
}
