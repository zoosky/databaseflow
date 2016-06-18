import models.{Ping, RequestMessage}
import services.{NavigationService, NotificationService}
import ui.modal.ReconnectManager
import utils.{JsonSerializers, NetworkSocket}

import scala.scalajs.js.timers._

trait NetworkHelper { this: DatabaseFlow =>
  private[this] var socket = new NetworkSocket(onSocketConnect, onSocketMessage, onSocketError, onSocketClose)

  protected[this] var latencyMs: Option[Int] = None

  protected def connect() = {
    socket.open(NavigationService.socketUrl)
  }

  private def sendPing(): Unit = {
    if (socket.connected) {
      utils.NetworkMessage.sendMessage(Ping(System.currentTimeMillis))
    }
    setTimeout(10000)(sendPing())
  }

  setTimeout(1000)(sendPing())

  protected[this] def onSocketConnect(): Unit = {
    //utils.Logging.info(s"Socket connected.")
  }

  protected[this] def onSocketError(error: String): Unit = {
    utils.Logging.error(s"Socket error [$error].")
  }

  protected[this] def onSocketClose(): Unit = {
    val callback = () => {
      utils.Logging.info("Attempting to reconnect.")
      socket.open(NavigationService.socketUrl)
    }
    ReconnectManager.show(callback, NotificationService.getLastError match {
      case Some(e) => s"${e._1}: ${e._2}"
      case None => "The connection to the server was closed."
    })
  }

  def sendMessage(rm: RequestMessage): Unit = {
    if (socket.connected) {
      val json = JsonSerializers.writeRequestMessage(rm, debug)
      socket.send(json)
    } else {
      throw new IllegalStateException("Not connected.")
    }
  }

  protected[this] def onSocketMessage(json: String): Unit = {
    val msg = JsonSerializers.readResponseMessage(json)
    handleMessage(msg)
  }
}
