package mohole.resources.script

import _root_.scails.{Utils, Resource}
import slinky.http.ContentType
import slinky.http.response.xhtml.Doctype.transitional

object ScriptResource extends Resource {
  val name = "scripts"

  def list = {
    Utils.say("hello")
  }
}