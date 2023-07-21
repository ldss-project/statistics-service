package io.github.jahrim.chess.statistics.service.components.data

/**
 * A sequence of [[Score]]s in chronological order, bound to a specific
 * owner in the leaderboard.
 *
 * @param username the name of the owner.
 * @param latestScores the latest scores of the owner.
 */
case class UserScoreHistory(username: String, latestScores: Seq[Score])
