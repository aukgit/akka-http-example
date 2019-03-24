package akkahttpexample

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akkahttpexample.JsonSupport._
import spray.json._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._


import scala.concurrent.{Await, Future}

/**
  * Actor taking care of configuring and starting the web server
  */
class HttpActor extends Actor {

  implicit val executionContext = context.dispatcher
  implicit val system = context.system
  implicit val timeout = Timeout(10,TimeUnit.SECONDS)
  implicit val materializer = ActorMaterializer()

  var http : HttpExt = null
  var binding : Future[ServerBinding] = null

  // The path to the actor that takes care of authenticating inbound messages
  val authenticateActor = context.actorSelection("/user/authenticateActor")

  // The path to the actor that takes care of registering a user
  val registerActor = context.actorSelection("/user/registerActor")

  override def receive: Receive = {
    // If a StartWebServerCommand is received, then start the web server
    case StartWebServerCommand =>
      if(http == null)
        startWebServer
    // If a StopWebServerCommand is received, then stop the web server
    case StopWebServerCommand =>
      if(binding != null)
        Await.result(binding, 10.seconds)
          .terminate(hardDeadline = 3.seconds)

  }

  /**
    * Configures and starts a web server
    */
  def startWebServer = {
    val routes : Route =
      // The endpoint that consumes a message to be delivered
      path("consume") {
        post {
          entity(as[Envelope]) { envelope =>
            // On success, forward envelope to the authenticateActor and await its verdict
            onSuccess(authenticateActor ? envelope){
              // If the authenticateActor returns an OpSuccess, then we're good and we print the message
              case res : OpSuccess => complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`,res.toJson.prettyPrint))
              // If the authenticateActor returns an OpFailure, then we're not good and we print the message
              case res : OpFailure => complete(StatusCodes.BadRequest,HttpEntity(ContentTypes.`application/json`, res.toJson.prettyPrint))
            }
          }
        }
      } ~
      // The endpoin that registers a user
      path("register") {
        post {
          entity(as[Registration]) { registration =>
                // Send the message to the registerActor and proceed further without waiting for the response
                registerActor ! registration
                // Return a confirmation message
                complete(HttpEntity(ContentTypes.`application/json`, "{\"done\":true}"))
          }
        }
      }

    // Send an asynchronous message to the logActor to say the web server is about to start
    context.actorSelection("/user/logActor") ! "Starting HTTP Server"
    // Start and bind the web server
    http = Http()
    binding = http.bindAndHandle(routes, "localhost", 8000)
  }
}
