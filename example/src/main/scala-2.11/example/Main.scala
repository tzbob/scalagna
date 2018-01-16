package example

import scalamt.macros._
import scalamt._

/**
  * Created by Michael on 6/04/2017.
  */
object Main extends MTApp {
  override def mtMain: List[MTPage] = {
    @server
    val _ = {
      import MTServerSettings._
      mtScalaJSName = "example"
      mtIsDevMode = true
    }

    List(
      GraffitiWebSockets,
      GraffitiRPC,
      ShoppingList
    )
  }
}
