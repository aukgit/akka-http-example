package akkahttpexample

import akka.actor.Actor

/**
  * Actor taking care of registering a user
  */
class RegisterActor extends Actor {

  /**
    * The path to the log actor
    */
  val logActor = context.actorSelection("/user/logActor")

  override def receive: Receive = {
    case msg : Registration =>
      logActor ! "Registering user "+msg.id

      // Loading the current users from file
      val data = FileUtil.loadUsersFromFile()
      // Setting the new user and saving the whole collection to file
      FileUtil.saveUsersToFile(data + (msg.id -> msg))
  }
}
