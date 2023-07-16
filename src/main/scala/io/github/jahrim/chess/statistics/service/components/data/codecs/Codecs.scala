package io.github.jahrim.chess.statistics.service.components.data.codecs

import org.bson.conversions.Bson

/** [[Bson]] codecs of the statistics service. */
object Codecs:
  export ScoreCodec.given
  export UserScoreCodec.given
