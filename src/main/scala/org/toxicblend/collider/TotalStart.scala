package org.toxicblend.collider

import org.toxicblend.collider.multiplex.MultiplexStart
import org.toxicblend.collider.slave.SlaveStart
import org.toxicblend.collider.client.ClientStart
import java.io.DataInputStream

object TotalStart {

  def main(args: Array[String]) = {
    MultiplexStart.main(args)
    Thread.sleep(1000)
    SlaveStart.main(args)
    Thread.sleep(1000)
    ClientStart.main(args)

    //val keyboard = new Scanner(System.in)
    val keyboard = new DataInputStream(System.in);

    var inputChar = ' '
    while (inputChar != 'q' && inputChar != 'Q') {
      inputChar = keyboard.readByte.toChar
    }
    Thread.sleep(500)

    ClientStart.shutdown
    SlaveStart.shutdown
    MultiplexStart.shutdown
  }
}
