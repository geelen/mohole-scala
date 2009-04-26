package scails


import slinky.http.response.Response
import xml.Elem

trait Resource {
  val name : String
  def list : Elem
}