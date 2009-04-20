package scails.edge

import collection.jcl.Conversions
import java.util.logging.Logger
import javax.jdo.JDOHelper
import scalaz.OptionW
import slinky.demo.PMF

//get rid of this and it fails below... :|
class JDO

object JDO {
  val logger = Logger.getLogger(classOf[JDO].getName)
  val pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");  

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