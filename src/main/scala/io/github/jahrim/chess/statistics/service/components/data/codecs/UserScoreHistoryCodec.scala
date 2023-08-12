package io.github.jahrim.chess.statistics.service.components.data.codecs

import ScoreCodec.given
import io.github.jahrim.chess.statistics.service.components.data.{Score, UserScoreHistory}
import io.github.jahrim.hexarc.persistence.bson.codecs.{
  BsonDocumentDecoder,
  BsonDocumentEncoder,
  BsonDecoder,
  BsonEncoder
}
import io.github.jahrim.hexarc.persistence.bson.dsl.BsonDSL.{*, given}
import org.bson.conversions.Bson

/** [[Bson]] codec for [[UserScoreHistory]]s. */
object UserScoreHistoryCodec:
  /** A given [[BsonDecoder]] for [[UserScoreHistory]]s. */
  given bsonToUserScoreHistory: BsonDocumentDecoder[UserScoreHistory] = bson =>
    UserScoreHistory(
      bson.require("username").as[String],
      bson.require("latestScores").as[Seq[Score]]
    )

  /** A given [[BsonEncoder]] for [[UserScoreHistory]]s. */
  given userScoreHistoryToBson: BsonDocumentEncoder[UserScoreHistory] = userScoreHistory =>
    bson {
      "username" :: userScoreHistory.username
      "latestScores" :: userScoreHistory.latestScores
    }
