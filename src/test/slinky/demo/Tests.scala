package slinky.demo

import fjs.test.Property._
import fjs.test.Bool._
import fj.test.CheckResult.summaryEx
import fj.test.Gen
import fj.test.Gen.{oneOf, elements, value}
import fjs.test.Arbitrary
import fjs.test.Arbitrary.{arb, arbitrary, arbAlphaNumString}
import fjs.data.List.ScalaList_List
import http.request.{Request, GET, Uri, Line, RequestHeader, Method}
import http.request.Uri.uri
import http.request.Line.line
import http.request.Request.request
import http.request.ArbitraryRequest
import http.request.ArbitraryRequestHeader.arbitraryRequestHeader
import http.request.ArbitraryUri.arbitraryUri
import http.response.{OK, BadRequest, MovedPermanently}
import http.ArbitraryVersion.arbitraryVersion
import http.{ArbitraryVersion, Version, ContentType}
import scalaz.StringW._
import scalaz.list.NonEmptyList
import scalaz.list.NonEmptyList.stringe
import scalaz.list.ArbitraryNonEmptyList._
import App._

object Tests {
  def getRequest(s: String) = ArbitraryRequest.arbitraryStreamRequest map (r => r(r(GET).line.uri(stringe(s))))

  def get(r: Request[Stream], s: String) = (r isGet) && (r pathEquals s)

  object HelloRequest {
    implicit val helloRequest = getRequest("/hello")

    val prop_hello = prop((r: Request[Stream]) =>
      (get(r, "/hello")) ->
        (app(r) exists (r => (r isOK) && (r contentTypeEquals "text/html"))))
  }

  object SayBadRequest {
    implicit val sayBadRequest = getRequest("/say")

    val prop_sayBadRequest = prop((r: Request[Stream]) =>
      ((get(r, "/say") && r ~!? "phrase")) -> (app(r) exists (_.isBadRequest)))
  }

  object SayOKRequest {
    implicit val sayOKRequest: fjs.test.Arbitrary[Request[Stream]] = for(r <- getRequest("/say");
                           p <- arbitrary[String]) yield
      r(r.line.uri ++++ (_ map (("phrase=" + p + '&').toList ::: _)))


    val prop_sayOKRequest = prop((r: Request[Stream]) =>
      (get(r, "/say") && r !? "phrase") ->
        (app(r) exists (rs => (rs isOK) && (rs.bodyString contains (r ! "phrase").get.mkString))))
  }

  object GoogleRequest {
    implicit val googleRequest = getRequest("/google")

    val prop_google = prop((r: Request[Stream]) =>
      (get(r, "/google")) ->
        (app(r) exists (_.status == MovedPermanently)))
  }

  object CountBadRequest {
    implicit val countBadRequest = getRequest("/countto")

    val prop_countBadRequest = prop((r: Request[Stream]) =>
      (get(r, "/countto") && r ~!? "n") ->
        (app(r) exists (rs => (rs isBadRequest) && (rs.bodyString contains "Pass the n request parameter"))))
  }

  object CountBadRequestParameter {
    implicit val countBadRequestParameter: fjs.test.Arbitrary[Request[Stream]] =
                       for(r <- getRequest("/countto");
                           p <- arbitrary[String]) yield
      r(r.line.uri ++++ (_ map (("n=" + p + '&').toList ::: _)))

    val prop_countBadRequestParameter = prop((r: Request[Stream]) => {
      val n = (r ! "n") map (_.parseInt)
      (get(r, "/countto") && (n exists (_.isLeft))) ->
        (app(r) exists (rs => (rs isBadRequest) && (rs.bodyString contains "NumberFormatException")))})

  }

  object CountOKRequest {
    implicit val countOKRequest: fjs.test.Arbitrary[Request[Stream]] =
                      for(r <- getRequest("/countto");
                          n <- arbitrary[Int]) yield
                        r({
                          val z = ("n=" + Math.abs(n) + '&').toList
                          r.line.uri(r.line.uri.queryString.map(z ::: _) orElse Some(z))
                        })


    val prop_countOKRequest = prop((r: Request[Stream]) => {
      (get(r, "/countto")) -> {
        val s = (r ! "n").get.mkString
        val n = s.parseInt.right.get
        val res = app(r)
        res exists (rs => rs.isOK &&
            res.get.bodyLength ==
            374 + n * 59 + s.length + ((1 to n) flatMap (_.toString) length))
      }
    })
  }

  object RequestRequest {
    implicit val requestRequest: fjs.test.Arbitrary[Request[Stream]] =
        for(r <- getRequest("/request");
            s <- arbitrary[String]) yield
          r(r.line.uri +++ (_ :::> s.toList))

    val prop_requestRequest = prop((r: Request[Stream]) =>
      ((r isGet) && (r pathStartsWith "/request")) ->
        (app(r) exists (_.isOK)))
  }

  val props = List(HelloRequest.prop_hello,
                   SayBadRequest.prop_sayBadRequest,
                   SayOKRequest.prop_sayOKRequest,
                   GoogleRequest.prop_google,
                   CountBadRequest.prop_countBadRequest,
                   CountBadRequestParameter.prop_countBadRequestParameter,
                   CountOKRequest.prop_countOKRequest,
                   RequestRequest.prop_requestRequest)

  def run = props foreach (p => summaryEx println p.check(100, 10000, 0, 100))

  def main(args: Array[String]) = run
}
