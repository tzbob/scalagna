package scalamt.macros

import scalamt._
import scalamt.macros.MacroLogger._
import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
  * Created by Michael on 28/01/2017.
  * JS - @server macro for the client compilation
  */
private object server {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    log("Executing @server macro for client compilation.")

    //TODO structures op een andere manier/globaal definieren
    val clientStructure = q"new client()"
    val serverStructure = q"new server()"
    val injectStructure = q"new inject()"
    val fragmentStructure = q"new fragment()"

    val id: Int = c.enclosingPosition.hashCode()

    val invalidAnnotationStructures: List[Tree] = List(
      clientStructure,
      serverStructure,
      fragmentStructure
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
        abort(c, "On a server definition, you can't use @fragment or both @server and @client annotations (or multiple instances of them).")

    //Check if the expression contains invalid annotations
    def checkExpression(expr: Tree): Unit =
      if (checkStructure(expr, invalidExpressionStructures))
        abort(c, "Inside a @server definition, the usage of @client, @server, @fragment and @inject annotations is not allowed.")

    //Check if the @inject annotation is included
    def isInjectable(mods: Modifiers): Boolean =
      checkStructures(mods.annotations, List(injectStructure))

    //Check if the modifiers has any of the invalid flags
    def checkInvalidFlags(mods: Modifiers): Boolean =
      invalidFlags.foldLeft(false)((has, flag) => if (has) has else mods.hasFlag(flag))

    //Perform all checks for the definition of variables - Return true if the variable is injectable
    def checkVariable(mods: Modifiers, expr: Tree, tpt: Tree): Boolean = {
      checkAnnotations(mods)
      checkExpression(expr)

      val injectable = isInjectable(mods)

      if (injectable) {
        //Check if type is specified
        if (tpt.isEmpty)
          abort(c, "When using @server together with @inject on a value definition, you must specify the type.")
        if (checkInvalidFlags(mods))
          abort(c, s"When using @server together with @inject on a value definition, it can't contain any of the following flags: IMPLICIT / LAZY.")
      }
      injectable
    }

    def checkDef(mods: Modifiers, expr: Tree, params: List[List[Tree]]) : Unit= {
      if (checkStructures(mods.annotations, List(clientStructure, injectStructure, serverStructure, fragmentStructure)))
        abort(c, "On a server def definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them).")
      if (params.exists(checkStructures(_, List(clientStructure, injectStructure, serverStructure, fragmentStructure))))
        abort(c, "On a server def definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them) in the parameters.")

    }

    def checkType(mods: Modifiers) : Unit=
      if (checkStructures(mods.annotations, List(clientStructure, injectStructure, serverStructure, fragmentStructure)))
        abort(c, "On a server type definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them).")


    val result = {
      annottees.map(_.tree).toList match {
        //VAL definition - @server val ... = ...
        case (cc@q"${mods: Modifiers} val ${tname: TermName}: ${tpt: Tree}= ${expr: Tree}") :: Nil =>
          //TODO Might change this to all terms that start with _ so multiple of these can be constructed in the application
          //If the variable name is _, the server expression can be removed in the client compilation
          if (tname.equals(TermName("_")))
            q""
          else {
            if (checkVariable(mods, expr, tpt)) //If the value is injectable, the type is specified
              q"$mods val $tname: _root_.scalamt.MTServerInjectable[$tpt] =  _root_.scalamt.MTAppInterface.serverI[$tpt]($id)"
            else
              q"$mods val $tname: _root_.scalamt.MTServer[Nothing] = _root_.scalamt.MTAppInterface.server()"
          }

        //VAR definition - @server var ... = ...
        case (cc@q"${mods: Modifiers} var $tname: ${tpt: Tree}= ${expr: Tree}") :: Nil =>
          //If the variable name is _, the server expression can be removed in the client compilation
          if (tname.equals(TermName("_")))
            q""
          else {
            if (checkVariable(mods, expr, tpt)) //If the value is injectable, the type is specified
              q"$mods val $tname: _root_.scalamt.MTServerInjectable[$tpt] = _root_.scalamt.MTAppInterface.serverI[$tpt]($id)"
            else
              q"$mods val $tname: _root_.scalamt.MTServer[Nothing] = _root_.scalamt.MTAppInterface.server()"
          }

        //DEF definition - @server def name (params): tpt = body
        case (cc@q"${mods: Modifiers} def $name[..$tparams](...${paramss: List[List[ValDef]]})(implicit ..$implparams): $tpt = ${body:Tree}") :: Nil =>
          checkDef(mods, body, paramss)
          if (isInjectable(mods)) {
            val wrappedParams = paramss.map((ls: List[ValDef]) => ls.map(valdef => {
              q"${valdef.mods} val ${valdef.name} : _root_.scalamt.MTServer[Nothing] = _root_.scalamt.MTAppInterface.server()"
            }))
            val res: DefDef = DefDef(mods, name, List(), wrappedParams, tq"_root_.scalamt.MTServer[Nothing]", q"_root_.scalamt.MTAppInterface.server()")
            res
          }
          else {
            val wrappedParams = paramss.map((ls: List[ValDef]) => ls.map(valdef => {
              q"${valdef.mods} val ${valdef.name} : _root_.scalamt.MTServer[Nothing] = _root_.scalamt.MTAppInterface.server()"
            }))
            val res: DefDef = DefDef(mods, name, List(), wrappedParams, tq"_root_.scalamt.MTServer[Nothing]", q"_root_.scalamt.MTAppInterface.server()")
            res
          }

        //TYPE definition - @server type x = tpt
        case (cc@q"${mods: Modifiers} type $tpname[..$tparams] = $tpt") :: Nil =>{
          checkType(mods)
          q""
        }

        case _ =>
          abort(c, "@server can only be used on a VAL / DEF or TYPE definition")
      }
    }
    info(c)
    c.Expr[Any](result)
  }
}

class server extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro server.impl
}


///*    println(annottees.size)
//
//    println(annottees.length)*/
//var logstr =""
//def log(str:String):Unit= logstr = s"$logstr\n $str"
//val result = {
//  annottees.map(_.tree).toList match {
//    case (cc@q"$mods val $tname: $tpt = $expr") :: Nil =>
//      /*          println("modifiers: " + mods.asInstanceOf[Modifiers])
//
//                /*          if (mods.asInstanceOf[Modifiers].annotations.nonEmpty)
//                            println(mods.asInstanceOf[Modifiers].annotations.exists(_.tpe =:= typeOf[inject]))*/
//                println("typeOf[inject]: " + typeOf[inject])
//                val annota: c.universe.Tree = q"new inject()"
//                println(showRaw(annota))
//
//                println(mods.asInstanceOf[Modifiers].annotations)
//                val t = mods.asInstanceOf[Modifiers].annotations
//                if (t.nonEmpty) {
//                  println("showRaw(t.head): " + showRaw(t.head))
//                  println("t.head.equalsStructure(annot): " + t.head.equalsStructure(annota))
//                  println("t.head.tpe: " + t.head.tpe) //null
//                  //println(t.head.tpe =:= typeOf[inject]) //nullpointer .tpe == null
//                  println("c.typecheck(t.head).tpe =:= typeOf[inject]: " + (c.typecheck(t.head).tpe =:= typeOf[inject])) //true
//
//                  mods.asInstanceOf[Modifiers].annotations match {
//                    case q"new inject()" :: _ => println("found")
//                    case _ =>
//                  }
//                }
//
//                val found = expr match {
//                  case q"$expr: @$annot" => true
//                  case _ => false
//                }
//
//                println("found: " + found)*/
//      println("exists: "+expr.exists(t => t.equalsStructure(q"new inject()")))
//      object traverser extends Traverser {
//        var result = List[Tree]()
//
//        override def traverse(tree: Tree): Unit = tree match {
//
//          case app@q"$expr: @$annot" =>
//            log("2: "+show(app))
//            result = annot :: result
//            super.traverse(expr)
//
//          case app@q"$mods val $tname: $tpt = $expr: @$annot" =>
//            log("3: annot: "+show(annot))
//            log("3: "+show(app))
//            result = mods.annotations ::: result
//            log(show(expr))
//            val eq1 = expr.equalsStructure(q"6: @inject")
//            val eq2 = expr match {
//              case app@q"$expr: @$annot" => result = annot :: result ; true
//              case _ => false
//            }
//            log("eq1: "+eq1)
//            log("eq2: "+eq2)
//            super.traverse(expr)
//
//          case _ => super.traverse(tree)
//        }
//      }
//      log("total expr: "+ show(expr))
//      traverser.traverse(expr)
//
//      log("result: "+traverser.result +" size: "+traverser.result.size)
//      //c.warning(c.enclosingPosition, "size: "+traverser.result.size)
//
//      /*          println(m + ", " + m.typeSignature + " -> " + m.accessed.annotations.foreach(a => println(a.tpe)))
//
//                println(m + " -> " + m.accessed.annotations.foreach(a => println(a.tpe))*/
//
//      /* println("tname: " + tname)
//       println("tpt: " + tpt)*/
//
//      if (tpt.isEmpty)
//        c.abort(c.enclosingPosition, "When using @server on val, you must specify the type")
//      print(logstr)
//      q"$mods val $tname: ServerMT[$tpt] = ServerMT($expr)"
//
//    case _ =>
//      c.abort(c.enclosingPosition, "@server must annotate a Val")
//  }
//}
//
//println("emptyline")
//c.Expr[Any](result)
//}