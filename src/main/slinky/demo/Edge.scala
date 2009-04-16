package slinky.demo

import collection.jcl.Conversions
import java.util.logging.Logger
import scalaz.OptionW

//get rid of this and it fails below... :|
class Edge

object Edge {
  val logger = Logger.getLogger(classOf[Edge].getName)
  
  def exec[A](query: String) : A = {
    val lol = OptionW.onull(PMF.pmfInstance.getPersistenceManager.newQuery(query).execute()) getOrElse {
      throw new IllegalArgumentException("Something is wrang with your query: " + query)
    }
    lol.asInstanceOf[A]
  }

  def execList[A](query: String) = {
    Conversions.convertList(exec[java.util.List[A]](query)).toList
  }
}