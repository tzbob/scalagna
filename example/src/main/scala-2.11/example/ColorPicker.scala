package example

import scalamt.macros._
import scalatags.Text.all._

/**
  * Created by Michael on 3/06/2017.
  * Colorpicker widget
  */
object ColorPicker {

  private val className: String = "colorpicker"
  private val width: Int = 284
  private val height: Int = 155

  @server
  def make: Frag = {
    canvas(`class` := className, style := "border: solid 1px #aaaaaa", widthA := width, heightA := height)
  }

  @client
  var selectedColor: (Int, Int, Int) = (0, 0, 0)

  /**
    * Initialise the colorpicker
    */
  @client
  def load(): Unit = {
    import org.scalajs.dom._
    val canvas = document.getElementsByClassName(className)(0).asInstanceOf[html.Canvas]
    val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

    /**
      * Disclaimer: The following code for the creation of a colorpicker canvas is a reproduction for ScalaJS from the code found on:
      * https://seesparkbox.com/foundry/how_i_built_a_canvas_color_picker
      */
    var gradient = ctx.createLinearGradient(0, 0, width, 0)
    // Create color gradient
    gradient.addColorStop(0, "rgb(255,   0,   0)")
    gradient.addColorStop(0.15, "rgb(255,   0, 255)")
    gradient.addColorStop(0.33, "rgb(0,     0, 255)")
    gradient.addColorStop(0.49, "rgb(0,   255, 255)")
    gradient.addColorStop(0.67, "rgb(0,   255,   0)")
    gradient.addColorStop(0.84, "rgb(255, 255,   0)")
    gradient.addColorStop(1, "rgb(255,   0,   0)")

    // Apply gradient to canvas
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, width, height)

    // Create semi transparent gradient (white -> trans. -> black)
    gradient = ctx.createLinearGradient(0, 0, 0, height)
    gradient.addColorStop(0, "rgba(255, 255, 255, 1)")
    gradient.addColorStop(0.5, "rgba(255, 255, 255, 0)")
    gradient.addColorStop(0.5, "rgba(0,     0,   0, 0)")
    gradient.addColorStop(1, "rgba(0,     0,   0, 1)")

    // Apply gradient to canvas
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, width, height)

    canvas.onclick = (e: MouseEvent) => {
      val bcr = canvas.getBoundingClientRect()
      val x = e.clientX - bcr.left
      val y = e.clientY - bcr.top
      val imageData = ctx.getImageData(x, y, 1, 1)
      selectedColor.value = (imageData.data(0), imageData.data(1), imageData.data(2))
    }
  }
}
