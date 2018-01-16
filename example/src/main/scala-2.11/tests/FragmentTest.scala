/*
package example

import scalamt._
import scalamt.macros.{client, fragment, inject, server}
import scalamt.services.MTService
import scalamt.MTPage
import scalatags.Text.all._
import scalatags.Text.tags2

/**
  * Created by Michael on 26/03/2017.
  */
object FragmentTest extends MTPage {

  override def execute(): List[MTService] = {

    @server
    val temp1 = 1

    @server @inject
    val temp4: Int = 5

    @client
    val temp3 = {
      //println(temp1.value) //test: non injectable server value used in client
      //println(temp4.value)
      5
    }

    @client @fragment
    val temp = (_, _) => println("test")

    @client @fragment
    val temp2 ={
      import org.scalajs.dom
      (_, _) => dom.window.alert("")
    }

    @server
    def bodyMain(): Seq[Frag] = {
      Seq(
        h1("Fragment Test"),
        button("Clear", id := "clear", onclick := temp),
        button("Clear2", id := "clear2", onclick := temp2)
      )
    }

    @server
    def headMain(): Seq[Frag] = {
      List(
        tags2.title("Page title")
      )
    }

    List(htmlService("/frag", bodyMain, headMain))
  }
}
*/
