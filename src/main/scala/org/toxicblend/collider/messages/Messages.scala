package org.toxicblend.collider.messages
import akka.actor.ActorRef

case class PrepareWork(client:ActorRef, settings:Map[String, String])
case class PrepareSlaveWorkers(settings:Map[String, String])
case object PrepareWorkAck 

case object SystemStatusQuery
case class SystemStatusResult(val availableWorkers:Int) 

case class RegisterSlaveManager(val aSlaveManager:ActorRef)
case object RegisterSlaveManagerAck

case class RegisterSlaveWorker(val aWorker:ActorRef) // ??
case object RegisterSlaveWorkerAck  // ??

case class WorkRequest(val from:ActorRef)
case class WorkResult(val from:ActorRef, val result:String) 
case class Work(val work:String)
