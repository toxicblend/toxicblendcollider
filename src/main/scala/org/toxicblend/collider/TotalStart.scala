package org.toxicblend.collider

import org.toxicblend.collider.multiplex.MultiplexStart
import org.toxicblend.collider.slave.SlaveStart
import org.toxicblend.collider.client.ClientStart

object TotalStart {
 
  def main(args: Array[String]) = {
    MultiplexStart.main(args)
    Thread.sleep(1000)
    SlaveStart.main(args)
    Thread.sleep(1000)
    ClientStart.main(args)
  }
}