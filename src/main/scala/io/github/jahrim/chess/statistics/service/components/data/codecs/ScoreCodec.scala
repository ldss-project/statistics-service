package io.github.jahrim.chess.statistics.service.components.data.codecs

import io.github.jahrim.chess.statistics.service.components.data.Score
import io.github.jahrim.hexarc.persistence.bson.codecs.{
  BsonDocumentDecoder,
  BsonDocumentEncoder,
  BsonDecoder,
  BsonEncoder
}
import io.github.jahrim.hexarc.persistence.bson.dsl.BsonDSL.{*, given}
import org.bson.conversions.Bson
import java.time.Instant

/** [[Bson]] codec for [[Score]]s. */
object ScoreCodec:
  /** A given [[BsonDecoder]] for [[Score]]s. */
  given bsonToScore: BsonDocumentDecoder[Score] = bson =>
    Score(
      bson.require("insertion").as[Instant],
      bson.require("rank").as[Long],
      bson.require("wins").as[Int],
      bson.require("losses").as[Int],
      bson.require("ratio").as[Double]
    )

  /** A given [[BsonEncoder]] for [[Score]]s. */
  given scoreToBson: BsonDocumentEncoder[Score] = score =>
    bson {
      "insertion" :: score.insertionDate
      "rank" :: score.rank
      "wins" :: score.wins
      "losses" :: score.losses
      "ratio" :: score.ratio
    }
