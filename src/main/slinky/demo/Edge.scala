package slinky.demo

import collection.jcl.Conversions
import java.util.logging.Logger
import scalaz.OptionW

//get rid of this and it fails below... :|
class Edge

object Edge {
  val logger = Logger.getLogger(classOf[Edge].getName)

  def exec[A](query: String): A = try {
    PMF.pmfInstance.getPersistenceManager.newQuery(query).execute().asInstanceOf[A]
  } catch {
    case e: NullPointerException =>
      throw new IllegalArgumentException("Something is wrang with your query: " + query, e)
  }

  def execList[A](query: String) = Conversions.convertList(exec[java.util.List[A]](query)).toList

  def save[A](obj: A) {
    val pm = PMF.pmfInstance.getPersistenceManager
    try {
      pm.makePersistent(obj);
    } finally {
      pm.close();
    }
  }
}