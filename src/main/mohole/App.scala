package mohole

import Function.curried
import java.util.Date
import java.util.logging.Logger
import scalaz.OptionW._
import scalaz.EitherW._
import scalaz.StringW._
import scalaz.control.MonadW.{EitherMonad, OptionMonad, EitherLeftMonad, ListMonad}
import slinky.demo.{RequestObj, Edge}

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

final class App extends StreamStreamServletApplication {
  import App._
  /*
  val application =
    (app(_: Request[Stream])) or (req => {
      implicit val r = req
      NotFound << transitional << say("Where does it lie?")
    })
    */

  val application = new ServletApplication[Stream, Stream] {
    def application(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[Stream]) =
      app getOrElse resource(x => OK << Stream.fromIterator(x), NotFound.xhtml << transitional << say("Where does it lie?"))
  }
}

import scalaz.CharSet.ISO8859
import slinky.http.servlet.HttpServletRequest.c

object App {
  implicit val charSet = ISO8859
  val logger: Logger = Logger.getLogger(classOf[App].getName)

  def app(implicit request: Request[Stream], servletRequest: HttpServletRequest) =
    c[Stream](request) match {
      case _ => None
    }

  def say[A](a: A) = doc("Mohole!", a)

  def doc[A](title: String, a: A) =
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>{ title }</title>
      </head>
      <body>
        { a }
      </body>
    </html>
}