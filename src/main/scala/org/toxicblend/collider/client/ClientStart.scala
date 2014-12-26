package org.toxicblend.collider.client

import akka.actor._
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.ActorSelection.toScala
import scala.concurrent.duration._

import com.typesafe.config.ConfigFactory
import org.toxicblend.collider.messages.PrepareWorkAck
import org.toxicblend.collider.messages.PrepareWork
import org.toxicblend.collider.messages.SystemStatusQuery
import org.toxicblend.collider.messages.SystemStatusResult
import org.toxicblend.collider.messages.Work
import org.toxicblend.collider.messages.WorkResult

import scala.language.postfixOps

object TellMeWhenYoureConfigured
object Configured

object ClientStart extends App {

  val config = ConfigFactory.load()
  val system = ActorSystem("clientSystem", config.getConfig("clientConf").withFallback(config))
  val timeout = Timeout(10 seconds)
  val workSettings = new collection.mutable.HashMap[String, String]

  val path = "akka.tcp://multiplexSystem@127.0.0.1:4224/user/multiplexManager"
  val localClient = ClientActor.start(system, path, workSettings.toMap, timeout)
  println("System should be up and running now")

  val rv = ClientActor.work(localClient, new Work("Solve Jacobian conjecture"), timeout)
  println("result is : " + rv.result)
  println("******************  client is done *********************")
}