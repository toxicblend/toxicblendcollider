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

class SlaveManager extends Actor with ActorLogging {
  
  val workers = new collection.mutable.ArrayBuffer[ActorRef]
  var sessionId = 0L
  
  def receive = {
    case RegisterSlaveManagerAck => {
       log.info("SlaveManager actor received RegisterSlaveManagerAck from " + sender)
       context become initiated(sender)
    }
    case x => log.info("******** received unknown message: " + x)
  }
  
  def initiated(multiplexor: ActorRef): Receive = {
    
    case ppsw:PrepareSlaveWorkers => {
      for (i <- 0 until 8){
         val aWorker = context.actorOf(Props(classOf[SlaveWorker], ppsw.settings), name = "slaveWorker" + sessionId + ":" + i )
         workers.append(aWorker)
         sender ! new WorkRequest(aWorker)
       }
      sessionId += 1
    }
    
    case str:String => {
      log.info("initiated SlaveManager received " + str)
    }
    
    case x => log.info("******** received unknown message: " + x)
  } 
}