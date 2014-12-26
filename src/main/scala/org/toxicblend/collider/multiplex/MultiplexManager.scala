package org.toxicblend.collider.multiplex

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import org.toxicblend.collider.messages.PrepareWorkAck
import org.toxicblend.collider.messages.PrepareWork
import org.toxicblend.collider.messages.RegisterSlaveManager
import org.toxicblend.collider.messages.RegisterSlaveManagerAck
import org.toxicblend.collider.messages.RegisterSlaveWorker
import org.toxicblend.collider.messages.PrepareSlaveWorkers
import org.toxicblend.collider.messages.RegisterSlaveWorkerAck
import org.toxicblend.collider.messages.SystemStatusQuery
import org.toxicblend.collider.messages.SystemStatusResult
import org.toxicblend.collider.messages.WorkRequest
import org.toxicblend.collider.messages.Work
import org.toxicblend.collider.messages.WorkResult

class WorkRequestCounter(var i: Int)

class WorkClientMapping(multiplexor: ActorRef, client: ActorRef)

class MultiplexManager extends Actor with ActorLogging {

  val slaveManagers = new collection.mutable.ArrayBuffer[ActorRef](10)
  val multiplexors = new collection.mutable.ArrayBuffer[WorkClientMapping](10)
  var multiplexCounter = 0L

  def receive = {

    case pw: PrepareWork => {
      log.info("MultiplexManager received PrepareWork: " + pw.client + " from " + sender)
      val m = context.actorOf(Props(classOf[Multiplexor], slaveManagers.toList), name = "multiplexor" + multiplexCounter)
      multiplexCounter += 1
      multiplexors.append(new WorkClientMapping(m, pw.client))
      m ! pw
    }

    case RegisterSlaveManager(aSlaveManager) => {
      slaveManagers.append(aSlaveManager)
      log.info("MultiplexManager received slaveManager: " + aSlaveManager + " from " + sender)
      aSlaveManager ! RegisterSlaveManagerAck
    }

    case SystemStatusQuery => {
      log.info("MultiplexManager received SystemStatusQuery from : " + sender)
      sender ! new SystemStatusResult(slaveManagers.size)
    }

    case x => log.info("******** received unknown message: " + x.toString)
  }
}

