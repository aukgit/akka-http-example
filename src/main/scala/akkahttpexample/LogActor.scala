package akkahttpexample

import java.util.Date

import akka.actor.Actor

/**
  * The simplest logging actor you can think of
  */
class LogActor extends Actor {

  override def receive: Receive = {
    case msg : String =>
      println(new Date()+") "+msg)
  }
}
