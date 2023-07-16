package io.github.jahrim.chess.statistics.service.components.ports

import io.github.jahrim.chess.statistics.service.components.exceptions.*
import io.github.jahrim.chess.statistics.service.components.data.{Score, UserScore}
import io.github.jahrim.hexarc.architecture.vertx.core.components.Port
import io.vertx.core.Future

/**
 * A [[Port]] that handles score submission and retrieval for
 * the users of a service.
 *
 * In the [[StatisticsPort]], a score keeps track of the wins
 * and losses of a user, supposedly obtained by participating
 * in different matches of a game.
 */
trait StatisticsPort extends Port:
  /**
   * Add the specified score to the scores of the specified user.
   *
   * @param username the name of the specified user.
   * @param hasWon   the specified score. True, if the user won the
   *                 match; false otherwise.
   * @return a [[Future]] completing when the specified score has
   *         been successfully added to the scores of the specified
   *         user.
   */
  def addScore(username: String, hasWon: Boolean): Future[Unit]

  /**
   * Delete the scores of the specified user.
   *
   * @param username the name of the specified user.
   * @return a [[Future]] completing when the scores of the specified
   *         user have been successfully deleted.
   *         The [[Future]] fails with a [[UserNotFoundException]]
   *         if no user with the specified name has been found among
   *         the users registered in the service.
   */
  def deleteScores(username: String): Future[Unit]

  /**
   * Get the latest score of the specified user.
   *
   * @param username the name of the specified user.
   * @return a [[Future]] containing the latest score of the specified
   *         user.
   *         The [[Future]] fails with a [[UserNotFoundException]]
   *         if no user with the specified name has been found among
   *         the users registered in the service.
   */
  def getScore(username: String): Future[Score]

  /**
   * Get all the scores of the specified user.
   *
   * @param username the name of the specified user.
   * @return a [[Future]] containing all the scores of the specified
   *         user.
   *         The [[Future]] fails with a [[UserNotFoundException]]
   *         if no user with the specified name has been found among
   *         the users registered in the service.
   */
  def getScoreHistory(username: String): Future[Seq[Score]]

  /**
   * Get the leaderboard of the service.
   *
   * @param first the highest rank in the leaderboard to query (included).
   * @param last  the lowest rank in the leaderboard to query (included).
   * @return a [[Future]] containing all the latest scores of all the
   *         users in the service between the specified ranks.
   * @note the scores are ordered by rank.
   */
  def getLeaderboard(first: Long, last: Long): Future[Seq[UserScore]]
