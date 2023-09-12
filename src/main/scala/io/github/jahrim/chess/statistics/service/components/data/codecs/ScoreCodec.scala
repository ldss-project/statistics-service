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
