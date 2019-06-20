package simulator.db.configuration

import java.util.UUID
import cats.effect.IO
import doobie.implicits._
import simulator.model.CategoricalConfig
import simulator.db.Storage
import doobie.postgres._
import doobie.postgres.implicits._

class CategoricalConfigurationStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id UUID NOT NULL,
        options UUID[] NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  def readByConfigurationId(id: UUID) =
    (sql"SELECT * FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[CategoricalConfig]
      .to[List]
      .transact(xa)

  def write(model: CategoricalConfig, configurationId: UUID) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, options)
          VALUES (${model.id}, $configurationId, ${model.options})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
}
