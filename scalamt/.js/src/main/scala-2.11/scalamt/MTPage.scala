package scalamt

import java.util.UUID

import scalamt.MTPage.MTPageID
import scalamt.services.MTService

/**
  * Created by Michael on 13/05/2017.
  * JS
  * A page represents a part of the full application.
  * For each HTML service created in a page, the server includes the same script.
  * This script executes the client version of this page using the execute method.
  * Application logic for a Page is added into the implementation of the execute method.
  * The services in the result of the execute method will be activated.
  */
trait MTPage {
  final implicit val page: MTPage = this
  final private[scalamt] val pageID: MTPageID = MTPage.getPageID

  private[scalamt] val uuid: UUID = UUID.randomUUID()
  private[scalamt] var injectables: Map[Int, String] = Map.empty

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
