package scails

import slinky.http.request.Request
import slinky.http.request.Request.Path

object ScailsHandler {
  def go(redirects: Map[String, String], resources: Resources)(lol: Request[Stream]) = {
    Utils.say(redirects.find((a: (String, String)) => {
      lol.pathEquals(a._1)
    }).map((a: (String, String)) => a._2))
  }
}