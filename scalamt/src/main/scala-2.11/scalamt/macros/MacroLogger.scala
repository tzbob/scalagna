package scalamt.macros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
  * Created by Michael on 27/03/2017.
  * Shared
  * Helper object for logging in the macro's.
  */
package object MacroLogger {
  private var logstr: String = ""

  private [scalamt] def log(str: Any): Unit = logstr = s"$logstr\n ${str.toString}"

  private [scalamt] def abort(c: whitebox.Context, msg: String): Nothing = {
    warning(c)
    c.abort(c.enclosingPosition, msg)
  }

  private [scalamt] def info(c: whitebox.Context, force: Boolean = true): Unit = {
    c.info(c.enclosingPosition, logstr, force)
    cleanLog()
  }

  private [scalamt] def warning(c: whitebox.Context): Unit = {
    c.warning(c.enclosingPosition, logstr)
    cleanLog()
  }

  private [scalamt] def cleanLog(): Unit =
    logstr = ""
}
