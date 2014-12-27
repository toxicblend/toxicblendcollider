package org.toxicblend.collider.multiplex

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import com.typesafe.config.ConfigFactory

object MultiplexStart extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("multiplexSystem", config.getConfig("multiplexConf").withFallback(config))
  val actor = system.actorOf(Props[MultiplexManager], name = "multiplexManager")
  println("MultiplexStart started")

  def shutdown = system.shutdown
}