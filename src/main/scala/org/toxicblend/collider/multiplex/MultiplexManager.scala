package org.toxicblend.collider.multiplex

import akka.actor._
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

class WorkRequestCounter(var i:Int)

class WorkClientMapping(multiplexor:ActorRef, client:ActorRef)

class MultiplexManager extends Actor with ActorLogging {
  
  val slaveManagers = new collection.mutable.ArrayBuffer[ActorRef](10)
  val multiplexors = new collection.mutable.ArrayBuffer[WorkClientMapping](10)
  var multiplexCounter = 0L
  
  def receive = {
    
    case pw:PrepareWork => {
      log.info("MultiplexManager received PrepareWork: " + pw.client + " from " + sender)
      val m = context.actorOf(Props(classOf[Multiplexor], slaveManagers.toList), name = "multiplexor" + multiplexCounter)
      multiplexCounter+=1
      multiplexors.append(new WorkClientMapping(m, pw.client))
      m ! pw
    }
       
    case RegisterSlaveManager(aSlaveManager) => {
      slaveManagers.append(aSlaveManager)
      log.info("MultiplexManager received slaveManager: " + aSlaveManager + " from " + sender )
      aSlaveManager ! RegisterSlaveManagerAck
    }
    
    case SystemStatusQuery => {
      log.info("MultiplexManager received SystemStatusQuery from : " + sender)
      sender ! new SystemStatusResult(slaveManagers.size)
    }
    
    case x => log.info("******** received unknown message: " + x.toString)
  }
}

/**
 * Multiplexor are instantiated one per work set
 */
class Multiplexor(val slaveManagers:Seq[ActorRef]) extends Actor with ActorLogging {
  
  val slaveWorkers = new collection.mutable.ArrayBuffer[ActorRef](10)
  val workRequests = new collection.mutable.PriorityQueue[ActorRef]
  val pendingWork = new collection.mutable.Queue[String]
  val workResult = new collection.mutable.ArrayBuffer[String](10)
  
  def assembleResult(client: ActorRef, expectedWorkSize:Int) = {
    if (workResult.size == expectedWorkSize)
      client ! new WorkResult(self, workResult.mkString(","))
  }
  
  def receive = {
    
    case pw:PrepareWork => {
      log.info("Multiplexor received PrepareWork: " + pw.client + " from " + sender)
      slaveManagers.foreach ( sm => sm ! new PrepareSlaveWorkers(pw.settings) )
      pw.client ! PrepareWorkAck
      context become initiated(pw.client)
    }
    
    case x => log.info("******** received unknown message: " + x.toString)
  }
  
  def initiated(client: ActorRef): Receive = {
    
    case wr:WorkRequest => {
      log.info("Multiplexor received WorkRequest from : " +wr.from )
      workRequests.enqueue(wr.from) 
    }
    
    case work:Work => {
      val workSize = 10
      log.info("initiated Multiplexor received Work from : " + sender )
      for (i <- 0 until workSize) {
        pendingWork.enqueue("part " + i + " of " + work.work)
      }
      if (workRequests.size > 0){
        workRequests.foreach( worker => { 
          if (pendingWork.size > 0) { 
            worker ! new Work(pendingWork.dequeue)
          }
        })
        workRequests.clear // This is cleary wrong
      } else {
        log.info("******** but there are no workers available")
      }
      context become working(client, workSize)
    }
    
    case x => log.info("initiated Multiplexor ******** received unknown message: " + x.toString)
  }
  
  def working(client: ActorRef, workSize:Int): Receive = {
    
    case wr:WorkResult => {
      log.info("initiated Multiplexor received WorkResult from : " + wr.from )
      workResult.append(wr.result)
      workRequests.enqueue(wr.from)
      if (pendingWork.size > 0)
        workRequests.dequeue ! new Work(pendingWork.dequeue)
      else {
        assembleResult(client, workSize)
      }
    }
    
    case wr:WorkRequest => {
      log.info("Multiplexor received WorkRequest from : " +wr.from )
      workRequests.enqueue(wr.from) 
      if (pendingWork.size > 0)
        workRequests.dequeue ! new Work(pendingWork.dequeue)
      else {
        assembleResult(client, workSize)
      }
    }
    
    case work:Work => {
      log.info("initiated Multiplexor received Work from : " + sender )
      for (i <- 0 until 10) {
        pendingWork.enqueue("part " + i + " of " + work.work)
      }
      if (workRequests.size > 0){
        workRequests.foreach( worker => { 
          if (pendingWork.size > 0) { 
            worker ! new Work(pendingWork.dequeue)
          }
        })
        workRequests.clear // This is cleary wrong
      } else {
        log.info("******** but there are no workers available")
      }
    }
    
    case x => log.info("initiated Multiplexor ******** received unknown message: " + x.toString)
  }
}

object MultiplexStart extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("multiplexSystem", config.getConfig("multiplexConf").withFallback(config))
  val actor = system.actorOf(Props[MultiplexManager], name = "multiplexManager")
  println("MultiplexStart started")
}