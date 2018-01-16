/*
package example

import scalamt._
import scalamt.macros._
import scalamt.services.MTService

/**
  * Created by Michael on 18/04/2017.
  */
object VarValTest extends MTPage {

  override def execute(): List[MTService] = {
    @server
    var test1 = 1

    @server
    val test2 = 2

    @server @inject
    var test3: Int = 3

    @server @inject
    val test4: Int = 4

    @client
    val test5 = {
      //test3.value = 6 //--> compile error
    }

    @server
    val _ = {
      //Server
      test1.value
      test1.value = test1.value + 1
      assert(test1.value == 2)

      test2.value
      //test2.value = 6 //--> compile error (val .value = ...)
      assert(test2.value == 2)

      test3.value
      test3.value = test3.value + 1
      assert(test3.value == 4)

      test4.value
      //test4.value = 6 //--> compile error (val .value = ...)
      assert(test4.value == 4)
    }
    List()
  }
}
*/
