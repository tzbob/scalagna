package scalamt.services

import org.http4s.dsl._
import org.http4s.{Request, Response}
import upickle.default._

import scala.util.Try
import scalaz.concurrent.Task

/**
  * Created by Michael on 25/02/2017.
  * JVM
  * Internal representation of a RPC Service of type [U,V].
  * 1. Client calls the service with an object of type U
  * 2. ScalaMT (Client): serialize this object and makes a POST call to the server on te given URL.
  * 3. ScalaMT (Server): deserialize the string to an object of type U and execute the given SERVER function using this object.
  * 4. ScalaMT (Server): serialize the result of type V and return this to the client.
  * 5. ScalaMT (Client): deserialize the string to an object of type V and execute the given CLIENT function using this object.
  */

case class MTServiceRPC[U: Reader : Writer, V: Reader : Writer] private[scalamt](url: String, sFunction: U => V) extends MTService {
  private [scalamt] def exec(req: Request): Task[Response] = {
    req.decode[String] { input =>
      Try {
        val i = read[U](input)
        Ok(write[V](sFunction(i)))
      }.getOrElse(BadRequest())
    }
  }
}
