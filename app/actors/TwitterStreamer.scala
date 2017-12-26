package actors


import akka.actor.{Actor,ActorRef, Props}
import play.api.Logger
import play.api.libs.json.Json




class TwitterStreamer(out: ActorRef) extends Actor {

  def receive= {
    case "subscribe" =>
      Logger.info("Received subscription from a client")
      out ! Json.obj("text" -> "Hello world!")

  }


}

object TwitterStreamer {

  //return Props of an ActorRef
  //the new is a little out of normal use. it must have checked the existance of actorref out.
  def props(out: ActorRef)= Props(new TwitterStreamer(out))


}