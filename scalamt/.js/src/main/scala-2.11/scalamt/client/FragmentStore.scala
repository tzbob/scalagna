package scalamt.client

import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js.annotation._
import scalamt.{IDException, MTClientF}

/**
  * Created by Michael on 25/03/2017.
  * JS
  * This object is used to store all the client fragments that are being created in the Page that is currently active.
  * The representation of a client fragment on the server, is the execution of the call method in JavaScript.
  * This method will then execute the correct client fragment.
  */

@JSExportTopLevel("FStore")
object FragmentStore {

  private val fragmentStore: mutable.Map[Int, (dom.Element, dom.Event) => Unit] = mutable.Map().empty

  @JSExport
  def call(element: dom.Element, event: dom.Event, id: Int): Unit = {
    fragmentStore.getOrElse(id, (_: dom.Element, _: dom.Event) => Unit)(element, event)
  }

  @throws[IDException]
  private [scalamt] def registerFragment(clientFragment: MTClientF[(dom.Element, dom.Event) => Unit]): Unit = {
    if(fragmentStore.contains(clientFragment.id))
      throw new IDException("Can't register the given fragment because a fragment with the same ID already exists.")
    fragmentStore += (clientFragment.id -> clientFragment.value)
  }
}
