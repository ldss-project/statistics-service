package io.github.jahrim.chess.statistics.service.components.data.codecs

import io.github.jahrim.chess.statistics.service.components.data.Score
import io.github.jahrim.hexarc.persistence.bson.codec.{BsonDecoder, BsonEncoder}
import io.github.jahrim.hexarc.persistence.bson.dsl.BsonDSL.{*, given}
import org.bson.conversions.Bson

/** [[Bson]] codec for [[Score]]s. */
object ScoreCodec:
  /** A given [[BsonDecoder]] for [[Score]]s. */
  given bsonToScore: BsonDecoder[Score] = bson =>
    Score(
      bson.require("date"),
      bson.require("rank"),
      bson.require("wins"),
      bson.require("losses"),
      bson.require("ratio")
    )

  /** A given [[BsonEncoder]] for [[Score]]s. */
  given scoreToBson: BsonEncoder[Score] = score =>
    bson {
      "date" :: score.date
      "rank" :: score.rank
      "wins" :: score.wins
      "losses" :: score.losses
      "ratio" :: score.ratio
    }
