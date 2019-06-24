package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.model.{Config, SimulationConfig}
import simulator.db.PrimaryConfigurationStorage
import doobie.postgres._
import doobie.postgres.implicits._

class Simulation(override val tableName: String) extends PrimaryConfigurationStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id UUID NOT NULL,
        start_time INTEGER NOT NULL,
        end_time INTEGER NOT NULL,
        number_of_customers INTEGER NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  override def readByOwnerId(id: UUID) = {
    println(s"sim store read ${id.toString}")
    (sql"SELECT id, start_time, end_time, number_of_customers FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[SimulationConfig]
      .unique
      .transact(xa)
  }

  override def write(config: Config, configurationId: UUID) = {
    val model = config.asInstanceOf[SimulationConfig]
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, start_time, end_time, number_of_customers)
          VALUES (${model.id}, $configurationId, ${model.startTime}, ${model.endTime}, ${model.numberOfCustomers})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
