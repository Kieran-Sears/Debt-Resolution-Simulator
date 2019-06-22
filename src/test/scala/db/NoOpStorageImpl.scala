package db

class NoOpStorageImpl {}

import java.util.UUID

import cats.Monad
import cats.effect.{Async, ContextShift, IO}
import cats.implicits._

class NoOpStorageImpl extends {
  val IO = Monad[IO]

  override def readAll(): IO[List[Message]] = IO.pure(Nil)
  override def readMessages(convId: UUID, userId: String): IO[List[Message]] =
    IO.pure(Nil)

  override def readNumberOfUnreadMessages(userId: String): IO[Int] = IO.pure(0)
}
