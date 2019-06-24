package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.model.{Config, CustomerConfig}
import simulator.db.PrimaryConfigurationStorage
import doobie.postgres._
import doobie.postgres.implicits._
import simulator.db.model.CustomerConfigData

class Customer(override val tableName: String) extends PrimaryConfigurationStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id  UUID NOT NUll,
        name text NOT NULL,
        proportion INTEGER NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  override def readByOwnerId(id: UUID) = {
    println(s"customer store read ${id.toString}")
    (sql"SELECT id, name, proportion FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[CustomerConfigData]
      .to[List]
      .transact(xa)
  }

  override def write(config: Config, configurationId: UUID) = {
    val model = config.asInstanceOf[CustomerConfig]
    println(s"Attempting to store CustomerConfig(${model.id}, $configurationId, ${model.name}, ${model.proportion})")
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, name, proportion)
          VALUES (${model.id}, $configurationId, ${model.name}, ${model.proportion})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
