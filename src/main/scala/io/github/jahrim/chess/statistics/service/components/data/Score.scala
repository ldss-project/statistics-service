/*
MIT License

Copyright (c) 2023 Cesario Jahrim Gabriele

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
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
   *               This is an [[Option]] containing true, if the user won the
   *               match; false otherwise. If the [[Option]] is empty, the
   *               result of the match will be registered in the scores of the
   *               user as a par.
   * @return a new [[Score]] obtained by adding the specified result to this score.
   */
  def addResult(hasWon: Option[Boolean]): Score =
    hasWon.map(hasWon => if hasWon then this.addVictory() else this.addLoss()).getOrElse(this)

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
