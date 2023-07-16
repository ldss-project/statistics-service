package io.github.jahrim.chess.statistics.service.components.exceptions

/**
 * A [[StatisticsServiceException]] triggered when a user is queried but not found.
 * @param username the name of the user.
 */
class UserNotFoundException(username: String)
    extends StatisticsServiceException(
      s"User not found: no user matches name '$username' in this service."
    )
