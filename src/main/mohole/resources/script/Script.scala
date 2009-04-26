package mohole.resources.script

import _root_.scails.{Utils, Resource, ResourceFactory}
import slinky.http.ContentType
import slinky.http.response.xhtml.Doctype.transitional

object Script extends ResourceFactory {
  def get() = new Resource {
    val name = "scripts"

    def list = {
      Utils.say("hello")
    }
  }
}

object ScriptResource extends Resource {
  val name = "scripts"

  def list = {
    Utils.say("hello")
  }
}