package io.github.jahrim.chess.statistics.service.components.ports

import io.github.jahrim.chess.statistics.service.components.data.{Score, UserScore}
import io.github.jahrim.hexarc.architecture.vertx.core.components.PortContext
import io.github.jahrim.hexarc.persistence.bson.PersistentDocumentCollection
import io.github.jahrim.hexarc.persistence.bson.dsl.BsonDSL.{*, given}
import io.vertx.core.Future

import java.time.ZonedDateTime

/**
 * Business logic of a [[StatisticsPort]].
 *
 * @param scoreCollection the [[PersistentDocumentCollection]] used by the
 *                        business logic to store the scores of the users.
 */
class StatisticsModel(scoreCollection: PersistentDocumentCollection) extends StatisticsPort:

  override protected def init(context: PortContext): Unit = {}

  override def addScore(username: String, hasWon: Boolean): Future[Unit] =
    context.vertx.executeBlocking[Unit] { promise =>
      context.log.info(s"Adding score { username: $username, hasWon: $hasWon }...")
      // todo
      promise.complete()
      context.log.info(s"Added score { username: $username, hasWon: $hasWon }.")
    }
  override def deleteScores(username: String): Future[Unit] =
    context.vertx.executeBlocking[Unit] { promise =>
      context.log.info(s"Deleting scores of '$username'...")
      // todo
      promise.complete()
      context.log.info(s"Deleted scores of '$username'.")
    }
  override def getScore(username: String): Future[Score] =
    context.vertx.executeBlocking[Score] { promise =>
      context.log.info(s"Retrieving scores of '$username'...")
      // todo
      promise.complete(Score(ZonedDateTime.now(), 0, 0, 0))
      context.log.info(s"Retrieved scores of '$username'.")
    }
  override def getScoreHistory(username: String): Future[Seq[Score]] =
    context.vertx.executeBlocking[Seq[Score]] { promise =>
      context.log.info(s"Retrieving score history of '$username'...")
      // todo
      promise.complete(Seq(Score(ZonedDateTime.now(), 0, 0, 0)))
      context.log.info(s"Retrieved score history of '$username'.")
    }
  override def getLeaderboard(first: Long, last: Long): Future[Seq[UserScore]] =
    context.vertx.executeBlocking[Seq[UserScore]] { promise =>
      context.log.info(s"Retrieving leaderboard [$first, $last]...")
      // todo
      promise.complete(Seq(UserScore("testUser", Score(ZonedDateTime.now(), 0, 0, 0))))
      context.log.info(s"Retrieved leaderboard [$first, $last].")
    }
