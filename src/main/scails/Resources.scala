package scails

import slinky.http.ContentType
import slinky.http.request.{GET, Request}
import slinky.http.response.{Response, OK}
import slinky.http.request.Request.Stream.MethodPath
import slinky.http.response.xhtml.Doctype.transitional
import slinky.http.StreamStreamApplication._
import slinky.http.servlet.StreamStreamServletApplication.resourceOr
import xml.Elem

class Resources(list : List[Resource]) {
  def get(implicit request: Request[Stream]) : Either[Elem, Elem] = {
    list.find((a : Resource) => request.pathStartsWith(a.name)).
            toRight(Utils.say("no resource found!")).right.map(act(request))
  }

  def act(request: Request[Stream])(resource: Resource) : Elem = {
    val listPath = "/" + resource.name + "/list"
    request match {
      case MethodPath(GET, listPath) => resource.list
      case _ => Utils.say("not found!")
    }
  }
}

object Resources {
  def from (list : List[Resource]) = new Resources(list)
}