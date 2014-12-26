package org.toxicblend.collider.multiplex

import akka.actor.Actor
import akka.actor.ActorSystem
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

/**
 * Multiplexor are instantiated one per work set
 */

class Multiplexor(val slaveManagers: Seq[ActorRef]) extends Actor with ActorLogging {

  val slaveWorkers = new collection.mutable.ArrayBuffer[ActorRef](10)
  val workRequests = new collection.mutable.PriorityQueue[ActorRef]
  val pendingWork = new collection.mutable.Queue[String]
  val workResult = new collection.mutable.ArrayBuffer[String](10)

  def assembleResult(client: ActorRef, expectedWorkSize: Int) = {
    if (workResult.size == expectedWorkSize)
      client ! new WorkResult(self, workResult.mkString(","))
  }

  def receive = {

    case pw: PrepareWork => {
      log.info("Multiplexor received PrepareWork: " + pw.client + " from " + sender)
      slaveManagers.foreach(sm => sm ! new PrepareSlaveWorkers(pw.settings))
      pw.client ! PrepareWorkAck
      context become initiated(pw.client)
    }

    case x => log.info("******** received unknown message: " + x.toString)
  }

  def initiated(client: ActorRef): Receive = {

    case wr: WorkRequest => {
      log.info("Multiplexor received WorkRequest from : " + wr.from)
      workRequests.enqueue(wr.from)
    }

    case work: Work => {
      val workSize = 10
      log.info("initiated Multiplexor received Work from : " + sender)
      for (i <- 0 until workSize) {
        pendingWork.enqueue("part " + i + " of " + work.work)
      }
      if (workRequests.size > 0) {
        workRequests.foreach(worker => {
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

  def working(client: ActorRef, workSize: Int): Receive = {

    case wr: WorkResult => {
      log.info("initiated Multiplexor received WorkResult from : " + wr.from)
      workResult.append(wr.result)
      workRequests.enqueue(wr.from)
      if (pendingWork.size > 0)
        workRequests.dequeue ! new Work(pendingWork.dequeue)
      else {
        assembleResult(client, workSize)
      }
    }

    case wr: WorkRequest => {
      log.info("Multiplexor received WorkRequest from : " + wr.from)
      workRequests.enqueue(wr.from)
      if (pendingWork.size > 0)
        workRequests.dequeue ! new Work(pendingWork.dequeue)
      else {
        assembleResult(client, workSize)
      }
    }

    case work: Work => {
      log.info("initiated Multiplexor received Work from : " + sender)
      for (i <- 0 until 10) {
        pendingWork.enqueue("part " + i + " of " + work.work)
      }
      if (workRequests.size > 0) {
        workRequests.foreach(worker => {
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
