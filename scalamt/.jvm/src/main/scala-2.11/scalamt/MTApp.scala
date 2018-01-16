package scalamt

import org.http4s.server.{Server, ServerApp}

import scalamt.server._
import scalaz.concurrent.Task

/**
  * Created by Michael on 6/04/2017.
  * JVM
  * Entrypoint for a ScalaMT application.
  * Extend a main object from this trait and implement the mtMain method.
  */
trait MTApp extends ServerApp {

  def mtMain: List[MTPage]

  override def server(args: List[String]): Task[Server] = {

    val pages: List[MTPage] = mtMain
    pages.foreach(_.init())
    checkServices(pages)
    println("--- Registered HTMLServices ---")
    pages.foreach(_.htmlServices.values.foreach(println))
    println("--- Registered RPCServices ---")
    pages.foreach(_.rpcServices.values.foreach(println))

    new MTServerMain(pages)
      .build()
      .start
  }

  @throws[IDException]
  private def checkServices(pages: List[MTPage]) = {
    val html = pages.flatMap(_.htmlServices.keys)
    val rpc = pages.flatMap(_.rpcServices.keys)

    val htmlDuplicates = html.diff(html.distinct).distinct
    val rpcDuplicates = rpc.diff(rpc.distinct).distinct

    if (htmlDuplicates.nonEmpty) throw new IDException(s"The usage of duplicate URL's for HTML Services is not allowed. Duplicates: ${htmlDuplicates.foldLeft("")((a,url:String) => a+s" $url")}")
    if (rpcDuplicates.nonEmpty) throw new IDException(s"The usage of duplicate URL's for RPC Services is not allowed.  Duplicates: ${rpcDuplicates.foldLeft("")((a,url:String) => a+s" $url")}")
  }
}

