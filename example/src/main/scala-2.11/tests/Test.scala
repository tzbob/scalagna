/*
package example


import scalamt._
import scalamt.macros._
import scalamt.services.{MTService}
import scalatags.Text.all.{div, _}

/**
  * Created by Michael on 24/04/2017.
  */
//@testt({import _root_.scala.collection.mutable.ListBuffer}, {import _root_.scala.collection.mutable.ListBuffer})
object Test extends MTPage {
  override def execute(): List[MTService] = {
    @server
    val _ = 5

    @server
    def testFunction(counter: Int, counter2: Int) = {
      //println(counter)
      6
    }

    @server
    val e = {
      testFunction(5, 5)
      (s: Int) => s
    }

    val rpcIncrease = rpcService[Int, Int]("/increasetest", e)

    @server
    def testFunction2[T](counter: Int)(counter2: T)()()(implicit e: Frag): Int = 5

    /*    @server @inject
        lazy val testlazy:Int = 5*/
    //Test1
    //import scala.collection.mutable.ListBuffer
    /*@client @imports
     val _ = {

     }
     @client
     val __ = {
       val yy: ListBuffer[Int] = ListBuffer.empty
     }
 */
    //Client: scalamt.MTServer does not take type parameters
    //Server if type deleted: trait MTServer takes type parameters
    def mkS(i: Int): MTServer[Int] = {
      //oplossing make def @server
      @server @inject
      val x: Int = i * i
      x
    }

    @client
    val x = 5


    // type variable client toevoege ipv int lelijke comp error, me reflection programma kapot (instanceof)

    //println(mkS(5).value) //Client: It is not allowed to retrieve a non-injectable server value (x.value) in a client or shared section.

    //Test2

    def mkS2(i: Int): Any = {
      @server @inject
      val x: Int = i
      x
    }

    /*@server @inject
    val nothingToStringFunction: Unit => String = () => "a string"
*/
    //println(mkS2(5).asInstanceOf[MTServer].value) //Client: It is not allowed to retrieve a non-injectable server value (x.value) in a client or shared section.

    /* @client
     val _2 ={
       for (i <- 1 to 6)
         println(mkS2(i).asInstanceOf[ServerInjectable[Int]].inject)
     }*/

    /*@server
    val _ = {
      for (i <- 1 to 5)
        println(mkS2(i).asInstanceOf[MTServer[Int]].value)
    }*/

    @client @fragment
    val onclickIncrease = {
      import org.scalajs.dom

      (_: dom.Element, _: dom.Event) => rpcIncrease.call(1)
    }

    @server
    val body: Seq[Frag] = Seq(
      div(
        button("Increase counter", id := "increase", onclick := onclickIncrease)
      )
    )

    val s = htmlService("/test",body)

    @server
    val body2: Seq[Frag] = Seq(
      div(
        a("Clickme", href := s.url)
      )
    )

    val s2 = htmlService("/test2",body2)

    List(s,s2)
  }

}
*/
