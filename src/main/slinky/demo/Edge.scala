package slinky.demo


import collection.jcl.Conversions

object Edge {
  def exec[A](query: String) : A = PMF.pmfInstance.getPersistenceManager.newQuery(query).execute().asInstanceOf[A]
  def execList[A <: List[A]](query: String) = Conversions.convertList(exec[java.util.List[A]](query)).toList
}