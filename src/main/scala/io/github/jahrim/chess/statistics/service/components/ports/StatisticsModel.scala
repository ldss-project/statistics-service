package io.github.jahrim.chess.statistics.service.components.ports

import io.github.jahrim.chess.statistics.service.components.data.codecs.Codecs.given
import io.github.jahrim.chess.statistics.service.components.data.{
  Score,
  UserScore,
  UserScoreHistory
}
import io.github.jahrim.chess.statistics.service.components.exceptions.*
import io.github.jahrim.hexarc.architecture.vertx.core.components.PortContext
import io.github.jahrim.hexarc.persistence.PersistentCollection
import io.github.jahrim.hexarc.persistence.bson.dsl.BsonDSL.{*, given}
import io.github.jahrim.hexarc.persistence.mongodb.language.MongoDBQueryLanguage
import io.github.jahrim.hexarc.persistence.mongodb.language.queries.{
  CreateQuery,
  DeleteQuery,
  ReadQuery,
  UpdateQuery
}
import io.vertx.core.Future
import org.bson.BsonDocument

import scala.util.{Failure, Success, Try}
import java.time.Instant

/**
 * Business logic of a [[StatisticsPort]].
 *
 * @param scores the [[PersistentCollection]] used by the business logic to
 *               store the scores of the users.
 */
class StatisticsModel(scores: PersistentCollection with MongoDBQueryLanguage)
    extends StatisticsPort:

  override protected def init(context: PortContext): Unit = {}

  override def addScore(username: String, hasWon: Option[Boolean]): Future[Unit] =
    context.vertx.executeBlocking[Unit] { promise =>
      activity(s"adding result '${resultString(hasWon)}' to the score of the user '$username'")(
        readActualScoreByUser(username)
          .transform(
            firstMatch => updateScoreHistoryByUser(username, firstMatch.addResult(hasWon)),
            noMatch => createScoreHistoryByUser(username, hasWon)
          )
          .flatMap(newScore => updateRankByUser(username, newScore))
          .get
      ).fold(
        exception => promise.fail(exception),
        success => promise.complete()
      )
    }

  override def deleteScores(username: String): Future[Unit] =
    context.vertx.executeBlocking[Unit](promise =>
      activity(s"deleting score of user '$username'")(
        readStatisticsByUser(username, bson { "username" :: 1 })
          .flatMap(userExists =>
            scores.delete(DeleteQuery(filter = bson { "username" :: username }))
          )
          .get
      ).fold(
        exception => promise.fail(exception),
        success => promise.complete()
      )
    )

  override def getScore(username: String): Future[Score] =
    context.vertx.executeBlocking[Score](promise =>
      activity(s"retrieving latest score of user '$username'")(
        readActualScoreByUser(username).get
      ).fold(
        exception => promise.fail(exception),
        latestScore => promise.complete(latestScore)
      )
    )

  override def getScoreHistory(username: String): Future[Seq[Score]] =
    context.vertx.executeBlocking[Seq[Score]](promise =>
      activity(s"retrieving score history of user '$username'")(
        readStatisticsByUser(username, bson { "latestScores" :: 1 })
          .map(_.require("latestScores").as[Seq[Score]])
          .flatMap(history => readActualScoreByUser(username).map(history :+ _))
          .get
      ).fold(
        exception => promise.fail(exception),
        latestScores => promise.complete(latestScores)
      )
    )

  override def getLeaderboard(first: Long, last: Long): Future[Seq[UserScore]] =
    context.vertx.executeBlocking[Seq[UserScore]](promise =>
      activity(s"retrieving leaderboard in [${boundaryString(first)}, ${boundaryString(last)}[")(
        readActualScores(first, last).get
      ).fold(
        exception => promise.fail(exception),
        latestScores => promise.complete(latestScores)
      )
    )

  /**
   * Create a new score history for the specified user, given the result of his first match.
   * @param username the name of the specified user.
   * @param hasWon the result of the first match of the user.
   * @return a [[Success]] containing the first [[Score]] of the user if the operation
   *         succeeded; a [[Failure]] otherwise.
   */
  private def createScoreHistoryByUser(username: String, hasWon: Option[Boolean]): Try[Score] =
    val newScore: Score = Score().addResult(hasWon)
    activity(s"initializing scores of user '$username' with result '${resultString(hasWon)}'")(
      scores
        .create(
          CreateQuery(document = UserScoreHistory(username, Seq(newScore)).asBson.asDocument)
        )
        .map(_ => newScore)
        .get
    )

  /**
   * Update the score history of the specified user by adding the specified
   * [[Score]] to it.
   *
   * @param username the name of the specified user.
   * @param newScore the specified [[Score]].
   * @return a [[Success]] containing the specified [[Score]] if the operation
   *         succeeded; a [[Failure]] otherwise.
   */
  private def updateScoreHistoryByUser(username: String, newScore: Score): Try[Score] =
    activity(s"updating scores of user '$username' with new score '$newScore'")(
      scores
        .update(
          UpdateQuery(
            filter = bson { "username" :: username },
            update = bson { "$push" :# { "latestScores" :: newScore } }
          )
        )
        .map(_ => newScore)
        .get
    )

  /**
   * Update the rank registered for the specified [[Score]] of the specified
   * user with the current actual rank of the user.
   *
   * @param username the name of the specified user.
   * @param newScore th specified [[Score]].
   * @return a [[Success]] containing the actual rank of the user if the
   *         operation succeeded; a [[Failure]] otherwise.
   */
  private def updateRankByUser(username: String, newScore: Score): Try[Long] =
    activity(s"updating rank of user '$username' with new score '$newScore'")(
      readActualScoreByUser(username)
        .map(_.rank)
        .flatMap(rank =>
          scores
            .update(
              UpdateQuery(
                filter = bson {
                  "username" :: username
                  "latestScores.insertion" :: newScore.insertionDate
                },
                update = bson { "$set" :# { "latestScores.$.rank" :: rank } }
              )
            )
            .map(_ => rank)
        )
        .get
    )

  /**
   * Read the information of the specified user selected by the specified
   * projection.
   *
   * @param username   the name of the specified user.
   * @param projection the specified projection.
   * @return a [[Success]] containing the information of the specified user
   *         selected by the specified projection; a [[Failure]] with a
   *         [[UserNotFoundException]] if the specified user does not exist
   *         in this service.
   */
  private def readStatisticsByUser(
      username: String,
      projection: BsonDocument = emptyBson
  ): Try[BsonDocument] =
    activity(s"retrieving statistics of user '$username'")(
      scores
        .read(
          ReadQuery(
            filter = bson { "username" :: username },
            projection = projection
          )
        )
        .flatMap(matches =>
          Try(matches.headOption.getOrElse(throw UserNotFoundException(username)))
        )
        .get
    )

  /**
   * Read the actual up-to-date [[UserScore]]s of the users of this service,
   * selecting only those between the specified ranks.
   *
   * @param first the rank of the first user to select (included).
   * @param last  the rank of the last user to select (excluded).
   * @return a [[Success]] containing the actual up-to-date [[UserScore]]s
   *         of the users of this service sorted by ascending rank;
   *         a [[Failure]] with an [[MalformedInputException]] if the rank
   *         of the last user to select isn't strictly greater than the rank
   *         of the first user to select.
   * @note the actual up-to-date [[UserScore]] of the user requires the actual
   *       up-to-date rank of the user, which does not depend only on the
   *       information of the user but also on that of the other users in
   *       this service. Therefore, there's a difference between the latest
   *       score inserted for the user and it's actual up-to-date [[UserScore]].
   */
  private def readActualScores(first: Long = 1, last: Long = Long.MaxValue): Try[Seq[UserScore]] =
    activity(s"retrieving actual scores in [${boundaryString(first)}, ${boundaryString(last)}[")(
      Try {
        if last <= first then
          throw MalformedInputException(
            "Last queried element of the leaderboard must be strictly greater than the first queried element."
          )
      }.flatMap(validBoundaries =>
        scores
          .read(
            ReadQuery.pipeline(
              bson {
                "$project" :# {
                  "username" :: 1
                  "score" :# { "$last" :: "$latestScores" }
                }
              },
              bson { "$sort" :# { "score.ratio" :: -1 } },
              bson { "$skip" :: (first - 1) },
              bson { "$limit" :: (last - first) }
            )
          )
      ).map(
        _.zipWithIndex
          .map((userScore, index) =>
            userScore
              .update {
                "score" :: userScore.require("score").asDocument.update {
                  "insertion" :: Instant.now
                  "rank" :: (first + index)
                }
              }
              .as[UserScore]
          )
      ).get
    )

  /**
   * Read the actual up-to-date [[Score]] of the specified user.
   *
   * @param username the name of the specified user.
   * @return a [[Success]] containing the actual up-to-date [[Score]]
   *         of the specified user; a [[Failure]] with a [[UserNotFoundException]]
   *         if the specified user does not exist in this service.
   */
  private def readActualScoreByUser(username: String): Try[Score] =
    activity(s"retrieving actual score of user '$username'")(
      readActualScores()
        .flatMap(ranks =>
          Try {
            ranks
              .find(_.username == username)
              .map(_.score)
              .getOrElse(throw UserNotFoundException(username))
          }
        )
        .get
    )

  /** The string representation of the result of a match. */
  private def resultString(hasWon: Option[Boolean]): String =
    hasWon.map(hasWon => if hasWon then "victory" else "loss").getOrElse("par")

  /** The string representation of a boundary in the leaderboard. */
  private def boundaryString(boundary: Long): String =
    if (boundary == Long.MaxValue) "inf" else boundary.toString

  /**
   * Log the start, failure and/or completion of the specified activity.
   *
   * @param activityName the name of the specified activity.
   * @param implementation     the implementation of the specified activity.
   */
  private def activity[T](activityName: String)(implementation: => T): Try[T] =
    context.log.info(s"Started: $activityName.")
    val result: Try[T] = Try(implementation)
    result match
      case Success(_) => context.log.info(s"Completed: $activityName.")
      case Failure(e) => context.log.info(s"Failed: $activityName.")
    result
