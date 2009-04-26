package scails

import slinky.http.request.Request
import slinky.http.request.Request.Path

object ScailsHandler {
  def go(redirects: Map[String, String], resources: Resources)(lol: Request[Stream]) = {
    Utils.say(Path.unapply(lol))
  }
}