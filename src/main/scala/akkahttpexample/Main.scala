package akkahttpexample

import akka.actor.{ActorSystem, Props}

object Main extends App {

    val actorSystem = ActorSystem.create("Foobar")
    val httpActor = actorSystem.actorOf(Props[HttpActor],"httpActor")
    val registerActor = actorSystem.actorOf(Props[RegisterActor], "registerActor")
    val authenticateActor = actorSystem.actorOf(Props[AuthenticateActor], "authenticateActor")
    val senderActor = actorSystem.actorOf(Props[SenderActor], "senderActor")
    val logActor = actorSystem.actorOf(Props[LogActor], "logActor")

    httpActor ! StartWebServerCommand
}
