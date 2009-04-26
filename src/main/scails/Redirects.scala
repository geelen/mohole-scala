package scails


import slinky.http.request.Request

class Redirects {
      def unapply(r: Request[Stream]): Option[(String)] =
        Some(List.toString(r.line.uri.path.toList))
}

object Redirects {
  def from(redirects: Map[String,String]) = {

  }
}