package scalamt

import scala.util.Properties.envOrNone

/**
  * Created by Michael on 12/05/2017.
  * Server settings - The values in this object can be changed in the mtMain method
  * of the application (... extends MTApp). These values will be read before the server application is loaded.
  */
object MTServerSettings {
  //Name of the scalaJs file
  var mtScalaJSName: String = "example"
  //If the SBT stage command is used, set mtIsDevMode to false
  //If the SBT ~re-start command is used, set mtIsDevMode to true
  var mtIsDevMode: Boolean = true
  //Resource directory
  var mtResourceDir: String = "static"
  //Host
  var mtHost: String = "localhost"
  //Port
  var mtPort: Int = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 8080
}
