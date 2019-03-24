package akkahttpexample

import akka.actor.Actor

/**
  * Actor taking care to verify whether the message sender is registered, and therefore allowed to send a message
  */
class AuthenticateActor extends Actor {

  /**
    * Path to the logActor
    */
  val logActor = context.actorSelection("/user/logActor")
  /**
    * Path to the senderActor
    */
  val senderActor = context.actorSelection("/user/senderActor")

  override def receive: Receive = {
    case msg : Envelope =>
      // Loading users from file
      val users = FileUtil.loadUsersFromFile()
      // If users contain the sender ID, we can proceed
      if(users.contains(msg.senderId)){
        logActor ! "Message from "+msg.senderId+" accepted"
        // Respond a success message to the sender (async)
        sender() ! new OpSuccess("Operation accepted")
        // The message is passed to the senderActor (async)
        senderActor ! msg
      } else {
        // User is not among the registered users
        logActor ! "Message from "+msg.senderId+" refused"
        // Respond a failure message to the sender (async)
        sender() ! new OpFailure("Sender is not registered")
      }
  }
}
