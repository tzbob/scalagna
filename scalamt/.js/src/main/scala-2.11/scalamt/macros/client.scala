package scalamt.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scalamt.macros.MacroLogger._

/**
  * Created by Michael on 29/01/2017.
  * JS - @client macro for the client compilation.
  */
object client {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    log("Executing @client macro for client compilation.")

    //TODO structures op een andere manier/globaal definieren
    val clientStructure = q"new client()"
    val serverStructure = q"new server()"
    val injectStructure = q"new inject()"
    val fragmentStructure = q"new fragment()"
    val importStructure = q"new imports()"

    val hash: Int = c.enclosingPosition.hashCode()

    val invalidAnnotationStructures: List[Tree] = List(
      clientStructure,
      serverStructure,
      injectStructure
    )

    val invalidExpressionStructures: List[Tree] = List(
      clientStructure,
      serverStructure,
      injectStructure,
      fragmentStructure
    )

    def check(mods: Modifiers, expr: Tree): Boolean = {
      //Check if the value definition contains invalid or duplicate annotations
      if (checkStructures(mods.annotations, invalidAnnotationStructures))
        abort(c, "On a client value definition, you can't use @inject or both @server and @client annotations (or multiple instances of them).")

      //Check if the expression contains invalid annotations
      if (checkStructure(expr, invalidExpressionStructures))
        abort(c, "Inside a @client value definition, the usage of @client, @server, @fragment and @inject annotations is not allowed.")

      //Check if this value definition has to be a fragment
      if (checkStructures(mods.annotations, List(fragmentStructure))) {
        if (mods.hasFlag(Flag.LAZY))
          abort(c, "When using @client together with @fragment on a value definition, it can't be lazy.")
        true
      } else false
    }

    def checkStructure(tree: Tree, structures: List[Tree]): Boolean =
      tree.exists(t => structures.exists(s => t.equalsStructure(s)))

    def checkStructures(trees: List[Tree], structures: List[Tree]): Boolean =
      trees.exists(t => checkStructure(t, structures))

    def checkDef(mods: Modifiers, expr: Tree, params: List[List[Tree]]) : Unit= {
      if (checkStructures(mods.annotations, List(clientStructure, injectStructure, serverStructure, fragmentStructure)))
        abort(c, "On a client def definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them).")
      if (params.exists(checkStructures(_, List(clientStructure, injectStructure, serverStructure, fragmentStructure))))
        abort(c, "On a client def definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them) in the parameters.")
    }

    val result = {
      annottees.map(_.tree).toList match {
        //Value definition - @client val ... = ...
        case (cc@q"${mods: Modifiers} val $tname: ${tpt: Tree} = ${expr: Tree}") :: Nil =>
          //If the variable name is _, the client expression will be wrapped with {} in the client compilation
          if (tname.equals(TermName("_")))
            expr
          else {
            val fragment: Boolean =
              check(mods, expr)

            if (fragment) {
                q"$mods val $tname: _root_.scalamt.MTClient[(_root_.org.scalajs.dom.Element, _root_.org.scalajs.dom.Event)=>Unit] =  _root_.scalamt.MTAppInterface.clientF($hash, $expr)"
            } else {
              if (tpt.isEmpty)
                q"$mods val $tname = _root_.scalamt.MTAppInterface.client($expr)"
              else
                q"$mods val $tname: _root_.scalamt.MTClient[$tpt] = _root_.scalamt.MTAppInterface.client[$tpt]($expr)"
            }
          }
        //Variable definition - @client var ... = ...
        case (cc@q"${mods: Modifiers} var $tname: ${tpt: Tree} = ${expr: Tree}") :: Nil =>
          //If the variable name is _, the client expression will be wrapped with {} in the client compilation
          if (tname.equals(TermName("_")))
            q"{$expr}"
          else {
            val fragment: Boolean =
              check(mods, expr)

            if (fragment)
              abort(c, "Only a immutable value (val) can be used as a fragment.")
             else {
              if (tpt.isEmpty)
                q"$mods val $tname = _root_.scalamt.MTAppInterface.clientV($expr)"
              else
                q"$mods val $tname: _root_.scalamt.MTClientVariable[$tpt] = _root_.scalamt.MTAppInterface.clientV[$tpt]($expr)"
            }
          }

        //Method definition - @client def ... = ...
        case (cc@q"${mods:Modifiers} def $name[..$tparams](...${paramss: List[List[ValDef]]}): $tpt = ${body:Tree}") :: Nil =>
          checkDef(mods, body, paramss)
          if (tpt.isEmpty)
            q"$mods def $name[..$tparams](...$paramss) = _root_.scalamt.MTAppInterface.client($body)"
          else
            q"$mods def $name[..$tparams](...$paramss): _root_.scalamt.MTClient[$tpt] = _root_.scalamt.MTAppInterface.client[$tpt]($body)"

        //TYPE definition - @client type x = tpt
        case (cc@q"${mods:Modifiers} type $tpname[..$tparams] = $tpt") :: Nil =>
          if(checkStructures(mods.annotations, invalidAnnotationStructures))
            abort(c, "On a client type definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them).")
          cc

        case _ =>
          abort(c, "@client can only be used on a VAL / DEF or TYPE definition")
      }
    }
    //log(result)
    info(c)

    c.Expr[Any](result)
  }
}

class client extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro client.impl
}