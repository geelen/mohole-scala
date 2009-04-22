package scails

import Function.curried
import java.util.Date
import java.util.logging.Logger
import scalaz.OptionW._
import scalaz.EitherW._
import scalaz.StringW._
import scalaz.control.MonadW.{EitherMonad, OptionMonad, EitherLeftMonad, ListMonad}
import slinky.http.response.{Response, OK, NotFound, BadRequest}
import slinky.http.servlet.{HttpServlet, HttpServletRequest, ServletApplication, StreamStreamServletApplication}
import slinky.http.servlet.HttpServlet._
import slinky.http.servlet.StreamStreamServletApplication.resourceOr
import slinky.http.StreamStreamApplication._
import slinky.http.request.Request.Stream.{MethodPath, Path}
import slinky.http.request.{Request, GET}
import slinky.http.response.xhtml.Doctype.{transitional, strict}
import slinky.http.response.StreamResponse.{response, statusLine}

import scalaz.CharSet.ISO8859
import slinky.http.servlet.HttpServletRequest.c
import slinky.http.{StreamStreamApplication, Application, ContentType}

class ScailsApp(redirects: Map[String, String], resources: Resources) extends StreamStreamServletApplication {
  import ScailsApp._

  val application = new ServletApplication[Stream, Stream] {
    def application(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[Stream]) =
      app getOrElse resource(x => OK << Stream.fromIterator(x), NotFound.xhtml << Utils.say("Where does it lie?"))
  }

  def app(implicit request: Request[Stream], servletRequest: HttpServletRequest) = {
    var lol = c[Stream](request)
    // FIX #3 Apr 20, 2009 ok this is LOL
    val matches: Iterable[Option[Response[Stream]]] = redirects.map((r: (String,String)) => lol match {
      case MethodPath(GET, r._1) => Some(redirect(r._2))
      case _ => None
    })
    matches.find(_.isDefined).orElse(resources.get)
  }
}

object ScailsApp {
  val charSet = ISO8859
  val logger: Logger = Logger.getLogger(classOf[ScailsApp].getName)
}