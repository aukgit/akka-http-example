package akkahttpexample

import akka.actor.Actor

/**
  * Actor handling the the messages being pushed to the system
  */
class SenderActor extends Actor {

  override def receive: Receive = {
    case msg : Envelope =>
        FileUtil.appendMessageToFile(msg)
  }

}
