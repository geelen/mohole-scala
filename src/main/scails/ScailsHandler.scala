package scails

import slinky.http.request.Request

object ScailsHandler {
  def go(redirects: Map[String, String], resources: Resources)(lol: Request[Stream]) = {
    Utils.say(lol.path)
  }
}