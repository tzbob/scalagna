package scalamt.services

import scalamt.MTInjectable
import scalatags.Text.RawFrag
import scalatags.Text.all._
import upickle.default._
import scalamt.MTServerSettings._
import scalamt.MTPage.MTPageID

/**
  * Created by Michael on 30/12/2016.
  * JVM
  * Helper object for constructing a HTML page. The wrap method builds the HTML page,
  * adds the injectable server variables into the page,
  * includes the correct JavaScript file/client application (full- or fast-opt.js) and
  * includes a script that executes the Page with the given ID.
  */
private[scalamt] object HTMLWrapper {
  private val useFullOpt = if (mtIsDevMode) "fast" else ""
  private val scalaJSAppString =
    s"/$mtResourceDir/$mtScalaJSName-${useFullOpt}opt.js"

  private[scalamt] def wrap(injectables: List[MTInjectable],
                            pageID: MTPageID,
                            _body: Seq[Frag],
                            _head: Seq[Frag]): Frag =
    html(
      head(
        inject(formatInjectables(injectables)),
        _head
      ),
      body(
        _body,
        scalajs(pageID)
      )
    )

  private def inject(injectables: String) =
    script(
      raw(
        s"//<![CDATA[ \n var __scalamt_request_data = '$injectables' \n //]]>")
    )

  private def scalajs(pageID: MTPageID): Seq[Tag] = Seq(
    script(src := scalaJSAppString),
    script(s"$mtScalaJSName.Main().main($pageID);")
  )

  private def formatInjectables(injectables: List[MTInjectable]): String = {
    if (injectables.nonEmpty)
      injectables
        .map(_.write)
        .foldLeft("")((a, b) => s"$a ${rep(b._1)}|${rep(b._2)}$$")
        .init
    else
      ""
  }

  private def rep(str: String): String =
    java.net.URLEncoder.encode(str, "utf-8")

}
