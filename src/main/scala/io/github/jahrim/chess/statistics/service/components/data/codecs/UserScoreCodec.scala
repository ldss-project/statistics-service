package io.github.jahrim.chess.statistics.service.components.data.codecs

import io.github.jahrim.chess.statistics.service.components.data.UserScore
import io.github.jahrim.hexarc.persistence.bson.codec.{BsonDecoder, BsonEncoder}
import io.github.jahrim.hexarc.persistence.bson.dsl.BsonDSL.{*, given}
import org.bson.conversions.Bson

/** [[Bson]] codec for [[UserScore]]s. */
object UserScoreCodec:
  import ScoreCodec.given

  /** A given [[BsonDecoder]] for [[UserScore]]s. */
  given bsonToUserScore: BsonDecoder[UserScore] = bson =>
    UserScore(
      bson.require("username"),
      bson.require("score")
    )

  /** A given [[BsonEncoder]] for [[UserScore]]s. */
  given userScoreToBson: BsonEncoder[UserScore] = userScore =>
    userScore.score.asBson.update { "username" :: userScore.username }
