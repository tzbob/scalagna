package scalamt.client

import upickle.default._

import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success, Try}
import scalamt.MTPage
import scalamt.MTPage.MTPageID

/**
  * Created by Michael on 6/04/2017.
  * JS
  * Start of a client application which collect the injectable server variables and executes the page with the given id.
  */
private[scalamt] object MTClientMain {
  def start(pageID: MTPageID, mtPages: List[MTPage]): Any = {
    mtPages.find(p => p.pageID.equals(pageID)) match {
      case Some(page: MTPage) =>
        Try {
          initializeInjectedVariables
        } match {
          case Success(v) =>
            page.injectables = v
            page.execute()
          case Failure(_) =>
            println("A problem occurred while initializing the injected server variables.")
        }
      case None =>
        println(s"The page with the following ID couldn't be found: $pageID")
    }
  }

  private def initializeInjectedVariables: Map[Int, String] = {
    val str = g.__scalamt_request_data
      .asInstanceOf[String]
    if (str.isEmpty)
      Map.empty
    else {
      str.split('$')
        .map(_.split('|').map(java.net.URLDecoder.decode(_, "utf-8")))
        .map(t =>
          (read[Int](t(0)), t(1))
        ).toMap[Int, String]
    }
  }
}
