package simulator.db.training

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.db.SecondaryTrainingStorage
import doobie.postgres._
import doobie.postgres.implicits._
import simulator.model.Train

class Effect(override val tableName: String) extends SecondaryTrainingStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id UUID NOT NULL,
        action_id UUID NOT NULL,
        name text NOT NULL,
        effect_type text NOT NULL,
        target text NOT NULL,
        value FLOAT,
        certainty FLOAT
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  override def readByOwnerId(id: UUID) =
    (sql"SELECT id, name, effect_type, target, value, certainty FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[Effect]
      .to[List]
      .transact(xa)

  override def write(train: Train, configurationId: UUID, actionId: UUID) = {
    val model = train.asInstanceOf[simulator.model.Effect]
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, action_id, name, effect_type, target, value, certainty)
          VALUES (${model.id}, $configurationId, $actionId, ${model.name}, ${model.effectType}, ${model.target}, ${model.value}, ${model.deviation})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
