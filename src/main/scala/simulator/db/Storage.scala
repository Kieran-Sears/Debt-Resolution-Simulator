package simulator.db
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import com.typesafe.scalalogging.LazyLogging
import doobie.util.ExecutionContexts
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import simulator.Settings

import scala.concurrent.ExecutionContext

trait Storage extends MetaMapping with LazyLogging {

  val tableName: String

  val tableNameFragment = Fragment.const(s"$tableName")

  def indexName(name: String) = Fragment.const(s"${tableName}_$name")

  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4)))

  val settings = Settings().DatabaseSettings

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    settings.driver,
    settings.simulatorUrl,
    settings.user, // user
    Settings().SecretSettings.dbSecret,
    ExecutionContexts.synchronous // just for testing
  )

  implicit val dbUrl: String = Settings().DatabaseSettings.simulatorUrl

}
