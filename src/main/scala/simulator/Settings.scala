package simulator

import akka.actor.Extension
import com.typesafe.config.{Config, ConfigFactory}

class Settings(globalConfig: Config) extends Extension {

  val akka: Config = globalConfig.getConfig("akka")

  private val databaseConfig: Config = globalConfig.getConfig("db")
  private val secretsConfig: Config = globalConfig.getConfig("secrets")

  object http {
    val interface: String = globalConfig.getString("http.interface")
    val port: Int = globalConfig.getInt("http.port")
  }

  object DatabaseSettings {
    val simulatorUrl: String = databaseConfig.getString("simulatorUrl")
    val userUrl: String = databaseConfig.getString("userUrl")
    val user: String = databaseConfig.getString("user")
    val driver: String = databaseConfig.getString("driver")
  }

  object SecretSettings {
    val dbSecret: String = secretsConfig.getString("dbSecret")
    val sessionSecret: String = secretsConfig.getString("sessionSecret")
  }
}

object Settings {
  def apply(): Settings = new Settings(ConfigFactory.load)
}
