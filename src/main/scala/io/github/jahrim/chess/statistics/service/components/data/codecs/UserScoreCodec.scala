package io.github.jahrim.chess.statistics.service.components.data.codecs

import ScoreCodec.given
import io.github.jahrim.chess.statistics.service.components.data.{Score, UserScore}
import io.github.jahrim.hexarc.persistence.bson.codecs.{
  BsonDocumentDecoder,
  BsonDocumentEncoder,
  BsonDecoder,
  BsonEncoder
}
import io.github.jahrim.hexarc.persistence.bson.dsl.BsonDSL.{*, given}
import org.bson.conversions.Bson

/** [[Bson]] codec for [[UserScore]]s. */
object UserScoreCodec:
  /** A given [[BsonDecoder]] for [[UserScore]]s. */
  given bsonToUserScore: BsonDocumentDecoder[UserScore] = bson =>
    UserScore(
      bson.require("username").as[String],
      bson.require("score").as[Score]
    )

  /** A given [[BsonEncoder]] for [[UserScore]]s. */
  given userScoreToBson: BsonDocumentEncoder[UserScore] = userScore =>
    userScore.score.asBson.asDocument.update { "username" :: userScore.username }
