package simulator

import akka.actor.Extension
import com.typesafe.config.{Config, ConfigFactory}

class Settings(globalConfig: Config) extends Extension {

  val akka: Config = globalConfig.getConfig("akka")
  val persistence: Config = akka.getConfig("persistence")
  val readJournal: String = persistence.getString("journal")
  val snapshotStore: String = persistence.getString("snapshot-store")

  object http {
    val interface: String = globalConfig.getString("http.interface")
    val port: Int = globalConfig.getInt("http.port")
  }
}

object Settings {
  def apply(): Settings = new Settings(ConfigFactory.load)
}