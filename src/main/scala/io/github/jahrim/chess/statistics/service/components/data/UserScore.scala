package io.github.jahrim.chess.statistics.service.components.data

/**
 * A [[Score]] bound to a specific owner in the leaderboard.
 *
 * @param username the name of the owner.
 * @param score the score of the owner.
 */
case class UserScore(username: String, private[data] val score: Score):
  export score.*
