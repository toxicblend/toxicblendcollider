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

class ClientActor extends Actor with ActorLogging {
  val commanders = new collection.mutable.ListBuffer[ActorRef]
  
  def receive = {
    
    case PrepareWorkAck => {
      log.info("uninitiaded ClientActor actor received PrepareWorkAck from multiplexor:" + sender)
      commanders.foreach ( c => { 
        c ! "Configured"
        log.info("Uninitiated ClientActor Sent Configured to " + c )
      })
      commanders.clear
      context become initiated(sender)
    }
    
    case TellMeWhenYoureConfigured => {
      log.info("uninitiaded ClientActor: TellMeWhenYoureConfigured: ClientActor appending " + sender)
      commanders.append(sender)
      log.info("Uninitiated ClientActor: commanders = " + commanders.mkString(","))
    }
    
    case x => log.info("Uninitiated ClientActor received unknown message: " + x)
  }
  
  def initiated(multiplexor: ActorRef): Receive = {
    
    case SystemStatusQuery => {
      log.info("Initiated ClientActor actor received: SystemStatusQuery" )
      multiplexor ! SystemStatusQuery
    }
    
    case ss:SystemStatusResult => {
      log.info("Initiated ClientActor actor received: " + ss)
    }
    
    case work:Work => {
      log.info("Initiated ClientActor actor received: " + work)
      commanders.append(sender)
      multiplexor ! work
    }
    
    case workResult:WorkResult => {
      log.info("Initiated ClientActor actor received: " + workResult)
      commanders.foreach ( c => { 
        c ! workResult
      })
      commanders.clear
    }
    
    case x => log.info("Initiated ClientActor received unknown message: " + x)
  } 

}

object ClientActor {
  def start(system:ActorSystem, path:String, workSettings:Map[String,String], aTimeout:Timeout):ActorRef = {
    val localClientActor = system.actorOf(Props[ClientActor], name = "client")
    val remoteMultiplexor = system.actorSelection(path)
    implicit val timeout = aTimeout   
    val aFuture = localClientActor ? TellMeWhenYoureConfigured // enabled by the “ask” import
    remoteMultiplexor ! new PrepareWork(localClientActor, workSettings) 
    Await.result(aFuture, timeout.duration)  
    localClientActor
  }
  
  def work(localClientActor:ActorRef, work:Work, aTimeout:Timeout) : WorkResult = {
    implicit val timeout = aTimeout
    val aFuture = localClientActor ? work
    val result = Await.result(aFuture, timeout.duration).asInstanceOf[WorkResult]
    result
  }
}

object ClientStart extends App {
  
  val config = ConfigFactory.load()
  val system = ActorSystem("clientSystem", config.getConfig("clientConf").withFallback(config))
  val timeout = Timeout(10 seconds)
  val workSettings = new collection.mutable.HashMap[String,String]
  
  val path = "akka.tcp://multiplexSystem@127.0.0.1:4224/user/multiplexManager"    
  val localClient = ClientActor.start(system,path,workSettings.toMap, timeout)
  println("System should be up and running now")
  
  val rv = ClientActor.work(localClient, new Work("Solve Jacobian conjecture"), timeout)
  println("result is : " + rv.result)
  println("******************  client is done *********************")
}