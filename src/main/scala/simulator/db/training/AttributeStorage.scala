package simulator.db.training

import java.util.UUID
import cats.effect.IO
import doobie.implicits._
import simulator.model.Attribute
import simulator.db.Storage
import doobie.postgres._
import doobie.postgres.implicits._

class AttributeStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id UUID NOT NULL,
        customer_id UUID NOT NULL,
        name text NOT NULL,
        value float NOT NULL
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

  def readByConfigurationId(id: UUID) =
    (sql"SELECT id, name, value FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[Attribute]
      .to[List]
      .transact(xa)

  def write(model: Attribute, configurationId: UUID, customerId: UUID) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, customer_id, name, value)
          VALUES (${model.id}, $configurationId, ${model.name}, ${model.value})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
}
