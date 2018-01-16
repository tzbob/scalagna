package scalamt.macros

import scalamt.macros.MacroLogger._
import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
  * Created by Michael on 28/01/2017.
  * JVM - @server macro for server compilation
  */
private object server {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    log( "Executing @server macro for server compilation.")

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

    def checkStructure(tree: Tree, structures: List[Tree]): Boolean =
      tree.exists(t => structures.exists(s => t.equalsStructure(s)))

    def checkStructures(trees: List[Tree], structures: List[Tree]): Boolean =
      trees.exists(t => checkStructure(t, structures))

    def check(mods: Modifiers, expr: Tree, tpt: Tree): Boolean = {
      //Check if the value definition contains invalid or duplicate annotations
      if (checkStructures(mods.annotations, invalidAnnotationStructures))
        abort(c, "On a server value definition, you can't use @fragment or both @server and @client annotations (or multiple instances of them).")

      //Check if the expression contains invalid annotations
      if (checkStructure(expr, invalidExpressionStructures))
        abort(c, "Inside a @server value definition, the usage of @client, @server, @fragment and @inject annotations is not allowed.")

      //Check if this value definition has to be injectable
      if (checkStructures(mods.annotations, List(injectStructure))) {
        //Check if type is specified
        if (tpt.isEmpty)
          abort(c, "When using @server together with @inject on a value definition, you must specify the type.")
        if(mods.hasFlag(Flag.LAZY))
          abort(c, "When using @server together with @inject on a value definition, it can't be lazy.")
        true
      } else false
    }

    def checkDef(mods: Modifiers, expr: Tree, params: List[List[Tree]]) : Unit= {
      if (checkStructures(mods.annotations, List(clientStructure, injectStructure, serverStructure, fragmentStructure)))
        abort(c, "On a client def definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them).")
      if (params.exists(checkStructures(_, List(clientStructure, injectStructure, serverStructure, fragmentStructure))))
        abort(c, "On a client def definition, you can't use @inject, @fragment or both @server and @client annotations (or multiple instances of them) in the parameters.")
    }

    val result = {
      annottees.map(_.tree).toList match {
        //Value definition - @server val ... = ...
        case (cc@q"${mods: Modifiers} val $tname: ${tpt: Tree}= ${expr: Tree}") :: Nil =>
          //If the variable name is _, the server expression will be wrapped with {} in the server compilation
          if (tname.equals(TermName("_")))
            q"{$expr}"
          else {
            val injectable: Boolean =
              check(mods, expr, tpt)

            if (injectable) //If the value is injectable, the type is specified
              q"$mods val $tname: _root_.scalamt.MTServer[$tpt] =  _root_.scalamt.MTAppInterface.serverI[$tpt]($id, $expr)"
            else if (tpt.isEmpty)
              q"$mods val $tname = _root_.scalamt.MTAppInterface.server($expr)"
            else
              q"$mods val $tname: _root_.scalamt.MTServer[$tpt] =  _root_.scalamt.MTAppInterface.server[$tpt]($expr)"
          }
        //Variable definition - @server var ... = ...
        case (cc@q"${mods: Modifiers} var $tname: ${tpt: Tree}= ${expr: Tree}") :: Nil =>
          //If the variable name is _, the server expression will be wrapped with {} in the server compilation
          if (tname.equals(TermName("_")))
            q"{$expr}"
          else {
            val injectable: Boolean =
              check(mods, expr, tpt)

            if (injectable) //If the value is injectable, the type is specified
              q"$mods val $tname: _root_.scalamt.MTServerVariable[$tpt] =  _root_.scalamt.MTAppInterface.serverIV[$tpt]($id, $expr)"
            else if (tpt.isEmpty)
              q"$mods val $tname = _root_.scalamt.MTAppInterface.serverV($expr)"
            else
              q"$mods val $tname: _root_.scalamt.MTServerVariable[$tpt] =  _root_.scalamt.MTAppInterface.serverV[$tpt]($expr)"
          }
        //Method definition - @server def ... = ...
        case (cc@q"${mods:Modifiers} def $name[..$tparams](...${paramss: List[List[ValDef]]}): $tpt = $body") :: Nil =>
          checkDef(mods, body, paramss)
          if(tpt.isEmpty)
            q"$mods def $name[..$tparams](...$paramss) = _root_.scalamt.MTAppInterface.server($body)"
          else
            q"$mods def $name[..$tparams](...$paramss): _root_.scalamt.MTServer[$tpt] = _root_.scalamt.MTAppInterface.server[$tpt]($body)"

        //TYPE definition - @server type x = tpt
        case (cc@q"${mods:Modifiers} type $tpname[..$tparams] = $tpt") :: Nil =>
          if(checkStructures(mods.annotations, invalidAnnotationStructures))
            abort(c, "On a server type definition, you can't use @inject or both @server and @client annotations (or multiple instances of them).")
          cc

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
//                            println(mods.asInstanceOf[Modifiers].annotations.exists(_.tpe =:= typeOf[macrosmt.inject]))*/
//                println("typeOf[macrosmt.inject]: " + typeOf[macrosmt.inject])
//                val annota: c.universe.Tree = q"new macrosmt.inject()"
//                println(showRaw(annota))
//
//                println(mods.asInstanceOf[Modifiers].annotations)
//                val t = mods.asInstanceOf[Modifiers].annotations
//                if (t.nonEmpty) {
//                  println("showRaw(t.head): " + showRaw(t.head))
//                  println("t.head.equalsStructure(annot): " + t.head.equalsStructure(annota))
//                  println("t.head.tpe: " + t.head.tpe) //null
//                  //println(t.head.tpe =:= typeOf[macrosmt.inject]) //nullpointer .tpe == null
//                  println("c.typecheck(t.head).tpe =:= typeOf[macrosmt.inject]: " + (c.typecheck(t.head).tpe =:= typeOf[macrosmt.inject])) //true
//
//                  mods.asInstanceOf[Modifiers].annotations match {
//                    case q"new macrosmt.inject()" :: _ => println("found")
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
//      println("exists: "+expr.exists(t => t.equalsStructure(q"new macrosmt.inject()")))
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
//            val eq1 = expr.equalsStructure(q"6: @macrosmt.inject")
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