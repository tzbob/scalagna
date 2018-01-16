package scalamt

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportDescendentObjects}
import scalamt.MTPage.MTPageID
import scalamt.client._
/**
  * Created by Michael on 6/04/2017.
  * JS
  * Entrypoint for a ScalaMT application.
  * Extend a main object from this trait and implement the mtMain method.
  */
@JSExportDescendentObjects
trait MTApp extends js.JSApp {

  def mtMain: List[MTPage]

  override def main(): Unit = {}

  @JSExport
  def main(pageID: MTPageID): Unit = {
    MTClientMain.start(pageID, mtMain)
  }
}
