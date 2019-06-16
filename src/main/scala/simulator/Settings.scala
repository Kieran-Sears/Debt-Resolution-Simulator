package simulator

import akka.actor.Extension
import com.typesafe.config.{Config, ConfigFactory}

class Settings(globalConfig: Config) extends Extension {

  val akka: Config = globalConfig.getConfig("akka")

  private val databaseConfig: Config = globalConfig.getConfig("db")

  object http {
    val interface: String = globalConfig.getString("http.interface")
    val port: Int = globalConfig.getInt("http.port")
  }

  object DatabaseSettings {
    val databaseUrl: String = databaseConfig.getString("url")
    val password: String = databaseConfig.getString("password")
  }
}

object Settings {
  def apply(): Settings = new Settings(ConfigFactory.load)
}
