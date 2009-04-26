package scails

import Function.curried
import java.util.Date
import java.util.logging.Logger
import scalaz.OptionW._
import scalaz.EitherW._
import scalaz.StringW._
import scalaz.control.MonadW.{EitherMonad, OptionMonad, EitherLeftMonad, ListMonad}
import slinky.http.servlet.{HttpServlet, HttpServletRequest, ServletApplication, StreamStreamServletApplication}
import slinky.http.servlet.HttpServlet._
import slinky.http.servlet.StreamStreamServletApplication.resourceOr
import slinky.http.{Application, ContentType}
import slinky.http.StreamStreamApplication._
import slinky.http.request.Request.Stream.{MethodPath, Path}
import slinky.http.request.{Request, GET}
import slinky.http.response.{OK, NotFound, BadRequest}
import slinky.http.response.xhtml.Doctype.{transitional, strict}
import slinky.http.response.StreamResponse.{response, statusLine}

class App(redirects: Map[String, String], resources: Resources) extends StreamStreamServletApplication {
  import App._

  val application = new ServletApplication[Stream, Stream] {
    def application(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[Stream]) =
      app(redirects: Map[String, String], resources: Resources) getOrElse resource(x => OK << Stream.fromIterator(x), NotFound.xhtml << transitional << Utils.say("Where does it lie?"))
  }
}

import scalaz.CharSet.ISO8859
import slinky.http.servlet.HttpServletRequest.c

object App {
  implicit val charSet = ISO8859
  val logger: Logger = Logger.getLogger(classOf[App].getName)

  def app(redirects: Map[String, String], resources: Resources)(implicit request: Request[Stream], servletRequest: HttpServletRequest) = {
    val lol = c[Stream](request)
    lol match {
      // 200 OK Say hello with XHTML Transitional
      case MethodPath(GET, "/hello") =>
        Some(OK(ContentType, "text/html") << transitional << Utils.say("hello"))

      // Echo the 'phrase' request parameter
      // or supply an appropriate error message with 400 Bad Request.
      case MethodPath(GET, "/say") => {
        val phrase = (request ! "phrase") > (_.mkString)
        Some(phrase ? (BadRequest, OK) << strict << Utils.say(phrase | "Pass the phrase request parameter"))
      }

      // Redirect to google.com
      case Path("/google") => Some(redirect("http://google.com/"))

      // Look for a resource with the given URI path.
      // If the resource does not exist, then 404 Not Found.
      case _ => {
        // FIX #1 Apr 26, 2009 there must be a better way to do this.
        if (redirects.contains(path)) {
          Some(redirect(redirects.get(path).get))
        } else {
          Some(OK(ContentType, "text/html") << transitional << ScailsHandler.go(redirects, resources)(lol))
        }
      }
    }
  }
}