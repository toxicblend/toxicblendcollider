package org.toxicblend.collider.slave

import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSelection.toScala
import org.toxicblend.collider.multiplex.MultiplexManager
import org.toxicblend.collider.messages.RegisterSlaveManager
import org.toxicblend.collider.messages.RegisterSlaveManagerAck
import org.toxicblend.collider.messages.RegisterSlaveWorker
import org.toxicblend.collider.messages.RegisterSlaveWorkerAck
import org.toxicblend.collider.messages.WorkRequest
import org.toxicblend.collider.messages.Work
import org.toxicblend.collider.messages.WorkResult
import org.toxicblend.collider.messages.PrepareSlaveWorkers

class SlaveWorker(val settings:Map[String, String]) extends Actor with ActorLogging {

  def receive = {  
    case wr:Work => {
      log.info("initiated SlaveWorker received WorkRequestAck" + wr)
      sender ! new WorkResult(self, wr.work.toUpperCase + " is solved " + settings)
    }
    case x => log.info("******** received unknown message: " + x)
  } 
}