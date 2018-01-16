package scalamt.services

import org.http4s.MediaType._
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`

import scalamt.MTPage
import scalatags.Text.all._
import scalaz.concurrent.Task

/**
  * Created by Michael on 25/02/2017.
  * JVM
  * Internal representation of a HTML Service.
  */
case class MTServiceHTML private[scalamt](url: String)(body: => Seq[Frag], head: => Seq[Frag])(private val page: MTPage) extends MTService {
  private[scalamt] def response: Task[Response] =
    Ok(
      HTMLWrapper
        .wrap(page.injectables.toList, page.pageID, body, head)
        .render
    ).putHeaders(`Content-Type`(`text/html`))
}
