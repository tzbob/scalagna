package scalamt.server

import java.util.concurrent.{ExecutorService, Executors}

import org.http4s.HttpService
import org.http4s.server.ServerBuilder
import org.http4s.server.blaze.BlazeBuilder
import org.log4s.{Logger, getLogger}

import scalamt.MTServerSettings.{mtHost, mtPort}
import scalamt.MTPage

/**
  * Created by Michael on 12/05/2017.
  * JVM
  * Build the internal webserver using the build method.
  */
private[scalamt] class MTServerMain(pages: List[MTPage]) {
  private val host: String = mtHost
  private val port: Int = mtPort
  private val logger: Logger = getLogger
  private val pool: ExecutorService = Executors.newCachedThreadPool()

  logger.info(s"Starting Http4s server on '$host:$port'")

  private val routes: HttpService =
    new Routes(pages).service

  private val service: HttpService =
    routes.local { req =>
      val path = req.uri.path
      logger.info(s"${req.remoteAddr.getOrElse("null")} -> ${req.method}: $path")
      req
    }

  def build(): ServerBuilder =
    BlazeBuilder
      .bindHttp(port, host)
      .mountService(service)
      .withServiceExecutor(pool)
}
