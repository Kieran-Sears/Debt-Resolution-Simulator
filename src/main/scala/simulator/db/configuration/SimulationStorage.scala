package simulator.db.configuration

import java.util.UUID
import cats.effect.IO
import doobie.implicits._
import simulator.model.SimulationConfig
import simulator.db.Storage
import doobie.postgres._
import doobie.postgres.implicits._

class SimulationStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
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

  def drop(): IO[Int] =
    for {
      queryResult <- (sql"DROP TABLE IF EXISTS " ++ tableNameFragment ++ sql";").update.run
        .transact(xa)
    } yield queryResult

  def readByConfigurationId(id: UUID) = {
    println(s"sim store read ${id.toString}")
    (sql"SELECT id, start_time, end_time, number_of_customers FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[SimulationConfig]
      .unique
      .transact(xa)
  }

  def write(model: SimulationConfig, configurationId: UUID) = {
    println(
      s"Attempting to store simulationConf(${model.id}, $configurationId, ${model.startTime}, ${model.endTime}, ${model.numberOfCustomers})")
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, start_time, end_time, number_of_customers)
          VALUES (${model.id}, $configurationId, ${model.startTime}, ${model.endTime}, ${model.numberOfCustomers})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
