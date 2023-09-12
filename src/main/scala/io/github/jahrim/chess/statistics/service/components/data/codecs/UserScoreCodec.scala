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
