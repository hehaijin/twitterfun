package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.oauth.{ ConsumerKey, RequestToken, OAuthCalculator }
import play.api.Play.current
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws._
import play.api.libs.iteratee._
import play.api.Logger
import scala.concurrent.duration._
import actors.TwitterStreamer
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.mvc.WebSocket.MessageFlowTransformer
import akka.stream.scaladsl._
import akka.util.ByteString

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class Application @Inject() (cc: ControllerComponents, config: Configuration, ws: WSClient)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[String, JsValue]
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def tweets2() = Action {
    request =>

      val LoggingIteratee = Iteratee.foreach[Array[Byte]] {
        array => Logger.info(array.map(_.toChar).mkString)

      }

      credentials.map {

        case (a, as, t, ts) => {
          ws.url("https://stream.twitter.com/1.1/statuses/filter.json")
            .sign(OAuthCalculator(ConsumerKey(a, as), RequestToken(t, ts)))
            .withQueryStringParameters("track" -> "trump")
            .withMethod("POST")
            .stream()
            .map {
              response =>
                Logger.info(response.status.toString)
                val source = response.bodyAsSource
                val sink = Sink.foreach((t: ByteString) => {
                  //Logger.info(getText(t))
                  println("new message")
                  // println(t.utf8String)

                })
                val runnable = source.toMat(sink)(Keep.right)
                val r = runnable.run()
                r.map(_ => println("the stream is closed"))
              // Ok("stream closed")
            }
        }
      } getOrElse {

        Future.successful(InternalServerError("credentials missing"))
      }

      Ok("ok")
  }

  def credentials = for {
    apiKey <- config.getString("twitter.apiKey")
    apiSecret <- config.getString("twitter.apiSecret")
    token <- config.getString("twitter.token")
    tokenSecret <- config.getString("twitter.tokenSecret")
  } yield (apiKey, apiSecret, token, tokenSecret)

 
  def tweets = WebSocket.acceptOrResult[String,String]{
    request =>
    val fsource=getTwitterSource(credentials.get)
    for(source <- fsource) yield Right(Flow.fromSinkAndSource(Sink.ignore, source))

  }

  def getText(bs: ByteString) = (Json.parse(bs.utf8String) \ "text").asOpt[String].getOrElse("no text")

  
  
  def getTwitterSource(credentials: (String, String, String, String)): Future[Source[String,_]] = {
  
     ws.url("https://stream.twitter.com/1.1/statuses/filter.json")
      .sign(OAuthCalculator(ConsumerKey(credentials._1, credentials._2), RequestToken(credentials._3, credentials._4)))
      .withQueryStringParameters("track" -> "trump")
      .withMethod("POST")
      .stream()
      .map {
        response => response.bodyAsSource.map(t=> t.utf8String)
      }
  }
  
  def index2()= Action{
    implicit request=>
    Ok(views.html.wsindex())
    
    
  }

}
