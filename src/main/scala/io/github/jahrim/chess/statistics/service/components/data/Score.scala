package io.github.jahrim.chess.statistics.service.components.data

import java.time.ZonedDateTime

/**
 * A score in the leaderboard.
 *
 * @param date the date when this score was registered.
 * @param rank the rank of the owner in the leaderboard.
 * @param wins the number of wins of the owner.
 * @param losses the number of losses of the owner.
 * @param ratio the ratio between wins and losses.
 * @note the ratio is taken as provided.
 */
case class Score private[data] (
    date: ZonedDateTime,
    rank: Long,
    wins: Int,
    losses: Int,
    ratio: Double
)

/** Companion object of [[Score]]. */
object Score:
  /**
   * @param date   the date when this score was registered.
   * @param rank   the rank of the owner in the leaderboard.
   * @param wins   the number of wins of the owner.
   * @param losses the number of losses of the owner.
   * @return a new [[Score]] with the specified statistics.
   * @note the ratio is inferred by the specified number of wins and losses.
   */
  def apply(date: ZonedDateTime, rank: Long, wins: Int, losses: Int): Score =
    Score(date, rank, wins, losses, if losses != 0 then wins / losses else wins)
