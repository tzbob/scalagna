package scalamt.services

/**
  * Created by Michael on 25/02/2017.
  * Shared - Global representation of a Service
  * All the services (HTML, RPC, WebSockets) extend from this trait.
  * It is NOT allowed to define a HTML/RPC Service with a duplicate URL.
  */
trait MTService {
  val url: String
}