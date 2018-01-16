package scalamt.macros

import scalamt.macros.MacroLogger._

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
  * Created by Michael on 29/01/2017.
  * JVM - @client macro for server compilation
  */
private object client {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    log("Executing @client macro for server compilation.")

    val clientStructure = q"new client()"
    val serverStructure = q"new server()"
    val injectStructure = q"new inject()"
    val fragmentStructure = q"new fragment()"

    val id: Int = c.enclosingPosition.hashCode()

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

    val invalidFlags: List[FlagSet] = List(Flag.IMPLICIT, Flag.LAZY)

    //Check if any of the structures is part of the given tree
    def checkStructure(tree: Tree, structures: List[Tree]): Boolean =
      tree.exists(t => structures.exists(s => t.equalsStructure(s)))

    //Check if any of the structures is part of any of the given trees
    def checkStructures(trees: List[Tree], structures: List[Tree]): Boolean =
      trees.exists(t => checkStructure(t, structures))

    //Check if the value definition contains invalid or duplicate annotations
    def checkAnnotations(mods: Modifiers): Unit =
      if (checkStructures(mods.annotations, invalidAnnotationStructures))
        abort(c, "On a client value definition, you can't use @inject or both @server and @client annotations (or multiple instances of them).")

    //Check if the expression contains invalid annotations
    def checkExpression(expr: Tree): Unit =
      if (checkStructure(expr, invalidExpressionStructures))
        abort(c, "Inside a @client value definition, the usage of @client, @server, @fragment and @inject annotations is not allowed.")

    //Check if the @fragment annotation is included
    def isFragment(mods: Modifiers): Boolean =
      checkStructures(mods.annotations, List(fragmentStructure))

    //Check if the modifiers has any of the invalid flags
    def checkInvalidFlags(mods: Modifiers): Boolean =
      invalidFlags.foldLeft(false)((has, flag) => if (has) has else mods.hasFlag(flag))


    def checkVariable(mods: Modifiers, expr: Tree, tpt: Tree): Boolean = {
      checkAnnotations(mods)
      checkExpression(expr)

      val fragment = isFragment(mods)

      if (fragment) {
        if (checkInvalidFlags(mods))
          abort(c, "When using @client together with @fragment on a value definition, it can't contain any of the following flags: IMPLICIT / LAZY.")
      }
      fragment
    }

    def checkDef(mods: Modifiers, expr: Tree, params: List[List[Tree]]) : Unit= {
      if (checkStructures(mods.annotations, List(clientStructure, injectStructure, serverStructure, fragmentStructure)))
        abort(c, "On a client def definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them).")
      if (params.exists(checkStructures(_, List(clientStructure, injectStructure, serverStructure, fragmentStructure))))
        abort(c, "On a client def definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them) in the parameters.")
    }

    def checkType(mods: Modifiers) : Unit=
      if (checkStructures(mods.annotations, List(clientStructure, injectStructure, serverStructure, fragmentStructure)))
        abort(c, "On a client type definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them).")


    val result = {
      annottees.map(_.tree).toList match {
        //Value definition - @client val ... = ...
        case (cc@q"${mods: Modifiers} val $tname: ${tpt: Tree} = ${expr: Tree}") :: Nil =>
          //If the variable name is _, the client expression can be removed in the server compilation
          if (tname.equals(TermName("_")))
            q""
          else {
            if (checkVariable(mods, expr, tpt))
              q"$mods val $tname: _root_.scalamt.MTClientFragment =  _root_.scalamt.MTAppInterface.clientF($id)"
            else
              q"$mods val $tname: _root_.scalamt.MTClient[Nothing] =  _root_.scalamt.MTAppInterface.client()"
          }
        //Variable definition - @client var ... = ...
        case (cc@q"${mods: Modifiers} var $tname: ${tpt: Tree} = ${expr: Tree}") :: Nil =>
          //If the variable name is _, the client expression can be removed in the server compilation
          if (tname.equals(TermName("_")))
            q""
          else {
            if (checkVariable(mods, expr, tpt))
              abort(c, "Only a immutable value (val) can be used as a client fragment.")
            else
              q"$mods val $tname: _root_.scalamt.MTClient[Nothing] =  _root_.scalamt.MTAppInterface.client()"
          }

        //DEF definition - @server def name (params): tpt = body
        case (cc@q"${mods: Modifiers} def $name[..$tparams](...${paramss: List[List[ValDef]]})(implicit ..$implparams): $tpt = ${body:Tree}") :: Nil =>
          checkDef(mods, body, paramss)
          val wrappedParams = paramss.map((ls: List[ValDef]) => ls.map(valdef => {
            q"${valdef.mods} val ${valdef.name} : _root_.scalamt.MTClient[Nothing] = _root_.scalamt.MTAppInterface.client()"
          }))
          val res: DefDef = DefDef(mods, name, List(), wrappedParams, tq"_root_.scalamt.MTClient[Nothing]", q"_root_.scalamt.MTAppInterface.client()")
          res

        //TYPE definition - @server type x = tpt
        case (cc@q"${mods: Modifiers} type $tpname[..$tparams] = $tpt") :: Nil =>{
          checkType(mods)
          q""
        }

        case _ =>
          abort(c, "@client can only be used on a VAL / DEF or TYPE definition")
      }
    }

    info(c)
    c.Expr[Any](result)
  }
}

class client extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro client.impl
}
