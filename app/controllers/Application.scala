package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.oauth.{ ConsumerKey, RequestToken, OAuthCalculator }
import play.api.Play.current
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws._
import play.api.Logger
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.stream.scaladsl.Framing


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

  def credentials = for {
    apiKey <- config.getString("twitter.apiKey")
    apiSecret <- config.getString("twitter.apiSecret")
    token <- config.getString("twitter.token")
    tokenSecret <- config.getString("twitter.tokenSecret")
  } yield (apiKey, apiSecret, token, tokenSecret)

 
  def tweets(topic: String) = WebSocket.acceptOrResult[String,String]{
    request =>
    val fsource=getTwitterSource(topic,credentials.get)
    for(source <- fsource) yield Right(Flow.fromSinkAndSource(Sink.ignore, source))

  }
 
  def getTwitterSource(topic: String, credentials: (String, String, String, String)): Future[Source[String,_]] = {
  
     ws.url("https://stream.twitter.com/1.1/statuses/filter.json")
      .sign(OAuthCalculator(ConsumerKey(credentials._1, credentials._2), RequestToken(credentials._3, credentials._4)))
      .withQueryStringParameters("track" -> topic)
      .withMethod("POST")
      .stream()
      .map {
        response => response.bodyAsSource.via(Framing.delimiter(ByteString.fromString("\n"), 20000)).map(t=> t.utf8String)
      }
  }
  
  def index()= Action{
    implicit request=>
    Ok(views.html.wsindex("Christmas"))
  }
  
  
  def stream(topic: String)= Action {
    
     implicit request=>
    Ok(views.html.wsindex(topic))
    
  }

}
