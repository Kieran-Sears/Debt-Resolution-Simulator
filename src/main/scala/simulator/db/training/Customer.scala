package simulator.db.training

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.db.model.CustomerData
import simulator.db.PrimaryTrainingStorage
import doobie.postgres._
import doobie.postgres.implicits._
import simulator.model.Train

class Customer(override val tableName: String) extends PrimaryTrainingStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id UUID NOT NULL,
        name text NOT NULL,
        difficulty FLOAT,
        assigned_label text
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  override def readByOwnerId(id: UUID) =
    (sql"SELECT id, name, difficulty, assigned_label FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[CustomerData]
      .to[List]
      .transact(xa)

  override def write(train: Train, configurationId: UUID) = {
    val model = train.asInstanceOf[simulator.model.Customer]
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, name, difficulty, assigned_label)
          VALUES (${model.id}, $configurationId, ${model.name}, ${model.difficulty}, ${model.assignedLabel},)
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
