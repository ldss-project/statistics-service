package io.github.jahrim.chess.statistics.service.components.exceptions

/**
 * An [[Exception]] triggered by the statistics service.
 * @param message a detailed description of the [[Exception]].
 */
class StatisticsServiceException(message: String) extends Exception(message)
