package slinky.demo

import Function.curried
import scalaz.OptionW._
import scalaz.EitherW._
import scalaz.StringW._
import scalaz.control.MonadW.{EitherMonad, OptionMonad, EitherLeftMonad, ListMonad}
import http.servlet.{HttpServlet, HttpServletRequest, ServletApplication, StreamStreamServletApplication}
import http.servlet.HttpServlet._
import http.servlet.StreamStreamServletApplication.resourceOr
import http.{Application, ContentType}
import http.StreamStreamApplication._
import http.request.Request.Stream.{MethodPath, Path}
import http.request.{Request, GET}
import http.response.{OK, NotFound, BadRequest}
import http.response.xhtml.Doctype.{transitional, strict}
import http.response.StreamResponse.{response, statusLine}

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
  
  def app(implicit request: Request[Stream], servletRequest: HttpServletRequest) =
    c[Stream](request) match {
      // 200 OK Say hello with XHTML Transitional
      case MethodPath(GET, "/hello") =>
        Some(OK(ContentType, "text/html") << transitional << say("hello"))

      // Echo the 'phrase' request parameter
      // or supply an appropriate error message with 400 Bad Request.
      case MethodPath(GET, "/say") => {
        val phrase = (request ! "phrase") > (_.mkString)
        Some(phrase ? (BadRequest, OK) << strict << say(phrase | "Pass the phrase request parameter"))
      }

      // Redirect to google.com
      case Path("/google") => Some(redirect("http://google.com/"))

      // Count to the given request parameter (n).
      // If no parameter, then 400 Bad Request with error message.
      // If parameter does not parse as numeric, then 400 Bad Request with exception error message.
      case MethodPath(GET, "/countto") => {
        val c = (request ! "n").toRight("Pass the n request parameter") >>=
            (s => s.mkString.parseInt.left > (_.toString))
        Some(c ? (BadRequest, OK) << transitional << (c fold (say(_), countto(_))))
      }

      // Display the details of the request.
      case Path(p) if p != "/norequest" => Some(request.debug[Stream](_.map(_.toChar).mkString))

      // Look for a resource with the given URI path.
      // If the resource does not exist, then 404 Not Found.
      case _ => None
    }

  def say[A](a: A) = doc("Slinky Demo", a)

  def doc[A](title: String, a: A) =
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>{ title }</title>
      </head>
      <body>
        { a }
      </body>
    </html>

  def countto(n: Int) = doc("Counting to " + n,
    <table border="1">
      <tr>
        <th>*</th>
        <th>n</th>
      </tr>
      {
        (1 to n).map(n =>
        <tr>
          <td>*</td>
          <td>{ n }</td>
        </tr>)
      }
    </table>)

  def accountForm =
    <form action="" method="post">
      <table border="0">
        {
          List(("First name", "first", "text", "First name or last name is required"),
               ("Last name", "last", "text", "First name or last name is required"),
               ("Favourite colour", "colour", "text", "Required"),
               ("Age", "age", "text", "Must be numeric and if the first name is only capital letters cannot be aged over 40"),
               ("Username", "username", "text", "Must be at least 6 characters"),
               ("Password", "password", "password", "Must be at least 8 characters"),
               ("Verify", "verify", "password", ""),
               ("Initial Amount $", "amount", "text", "Must be numeric or decimal amount")
              ) >
          {
            case (label, name, t, message) =>
            <tr>
              <td>{ label }</td>
              <td><input type={ t } name={ name }/> <i>{ message }</i></td>
            </tr>
          }
        }
      </table>
      <input type="submit"/>
    </form>
}