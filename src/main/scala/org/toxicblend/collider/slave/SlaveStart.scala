package org.toxicblend.collider.slave

import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import org.toxicblend.collider.messages.RegisterSlaveManager
import org.toxicblend.collider.messages.RegisterSlaveManagerAck

object SlaveStart extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("slaveSystem", config.getConfig("slaveConf").withFallback(config))
  val slaveManager = system.actorOf(Props[SlaveManager], name = "slaveManager")
  val multiplexManager = system.actorSelection("akka.tcp://multiplexSystem@127.0.0.1:4224/user/multiplexManager")
  multiplexManager ! new RegisterSlaveManager(slaveManager)
  
  //remoteMultiplexor ! "Test message"
}