package slinky.demo

import _root_.javax.jdo.JDOHelper

object PMF {
  val pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");
}