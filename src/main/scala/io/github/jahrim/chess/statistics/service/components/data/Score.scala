package io.github.jahrim.chess.statistics.service.components.data

import java.time.Instant

/**
 * A score in the leaderboard.
 *
 * @param insertionDate the date when this score was registered.
 * @param rank the rank of the owner in the leaderboard.
 * @param wins the number of wins of the owner.
 * @param losses the number of losses of the owner.
 * @param ratio the ratio between wins and losses.
 * @note the ratio is taken as provided.
 */
case class Score private[data] (
    insertionDate: Instant,
    rank: Long,
    wins: Int,
    losses: Int,
    ratio: Double
):
  /**
   * Add the specified result to this [[Score]].
   *
   * @param hasWon the specified result.
   * @return a new [[Score]] obtained by adding the specified result to this score.
   */
  def addResult(hasWon: Boolean): Score = if hasWon then this.addVictory() else this.addLoss()

  /**
   * Add a victory to this [[Score]].
   *
   * @return a new [[Score]] obtained by adding a victory to this score.
   */
  private def addVictory(): Score = Score(wins = this.wins + 1, losses = this.losses)

  /**
   * Add a loss to this [[Score]].
   *
   * @return a new [[Score]] obtained by adding a loss to this score.
   */
  private def addLoss(): Score = Score(wins = this.wins, losses = this.losses + 1)

/** Companion object of [[Score]]. */
object Score:
  /** The value of a rank that is not known yet. */
  private val UnknownRank: Long = Long.MaxValue

  /**
   * @param insertionDate the date when this score was inserted.
   * @param rank          the rank of the owner in the leaderboard.
   * @param wins          the number of wins of the owner.
   * @param losses        the number of losses of the owner.
   * @return a new [[Score]] with the specified statistics.
   * @note the ratio is inferred by the specified number of wins and losses.
   */
  def apply(
      insertionDate: Instant = Instant.now,
      rank: Long = UnknownRank,
      wins: Int = 0,
      losses: Int = 0
  ): Score =
    Score(insertionDate, rank, wins, losses, if losses != 0 then wins.toDouble / losses else wins)
