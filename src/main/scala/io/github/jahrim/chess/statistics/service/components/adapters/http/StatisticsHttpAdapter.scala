package io.github.jahrim.chess.statistics.service.components.adapters.http

import io.github.jahrim.chess.statistics.service.components.adapters.http.StatisticsHttpAdapter.*
import io.github.jahrim.chess.statistics.service.components.adapters.http.StatisticsHttpAdapter.given
import io.github.jahrim.chess.statistics.service.components.exceptions.*
import io.github.jahrim.chess.statistics.service.components.data.codecs.Codecs.given
import io.github.jahrim.chess.statistics.service.components.data.codecs.JsonObjectCodec.*
import io.github.jahrim.chess.statistics.service.components.adapters.http.handlers.LogHandler
import io.github.jahrim.chess.statistics.service.components.ports.StatisticsPort
import io.github.jahrim.hexarc.persistence.bson.dsl.BsonDSL.{*, given}
import io.github.jahrim.hexarc.architecture.vertx.core.components.{Adapter, AdapterContext}
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.{Router, RoutingContext}
import io.vertx.ext.web.handler.{BodyHandler, CorsHandler}
import io.vertx.core.http.{HttpMethod, HttpServerOptions, HttpServerResponse}
import org.bson.{BsonDocument, BsonValue}

import scala.jdk.CollectionConverters.{SeqHasAsJava, SetHasAsJava}
import scala.util.Try

/** An [[Adapter]] that enables http communication with a [[StatisticsPort]] of a service. */
class StatisticsHttpAdapter(
    httpOptions: HttpServerOptions = HttpServerOptions().setHost("localhost").setPort(8080),
    allowedOrigins: Seq[String] = Seq()
) extends Adapter[StatisticsPort]:

  override protected def init(context: AdapterContext[StatisticsPort]): Unit =
    val router: Router = Router.router(context.vertx)

    val cors: CorsHandler =
      CorsHandler
        .create()
        .addOrigins(allowedOrigins.asJava)
        .allowCredentials(true)
        .allowedMethods(
          Set(
            HttpMethod.HEAD,
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.PUT
          ).asJava
        )

    router
      .route()
      .handler(cors)
      .handler(BodyHandler.create())
      .handler(LogHandler(context.log.info))
      .failureHandler(context => context.sendException(context.failure))

    router
      .get("/")
      .handler(_.response.send("Welcome to the Statistics Service!"))

    router
      .post("/score/:username")
      .handler(message =>
        val username: String = message.requirePathParam("username")
        val hasWon: Option[Boolean] =
          jsonToBson(message.body.asJsonObject)("score.hasWon").map(_.as[Boolean])
        context.api
          .addScore(username, hasWon)
          .onSuccess(_ => message.sendOk())
          .onFailure(message.sendException)
      )

    router
      .delete("/score/:username")
      .handler(message =>
        val username: String = message.requirePathParam("username")
        context.api
          .deleteScores(username)
          .onSuccess(_ => message.sendOk())
          .onFailure(message.sendException)
      )

    router
      .get("/score/:username")
      .handler(message =>
        val username: String = message.requirePathParam("username")
        context.api
          .getScore(username)
          .map(score => bson { "score" :: score })
          .onSuccess(bson => message.sendBson(HttpResponseStatus.OK, bson))
          .onFailure(message.sendException)
      )

    router
      .get("/score/history/:username")
      .handler(message =>
        val username: String = message.requirePathParam("username")
        context.api
          .getScoreHistory(username)
          .map(scores => bson { "scores" :: scores })
          .onSuccess(bson => message.sendBson(HttpResponseStatus.OK, bson))
          .onFailure(message.sendException)
      )

    router
      .get("/leaderboard")
      .handler(message =>
        val first: Long =
          Try(message.queryParam("first").get(0))
            .map(_.toLong)
            .getOrElse(1)
        val last: Long =
          Try(message.queryParam("last").get(0))
            .map(_.toLong)
            .getOrElse(first + DefaultLeaderboardWindowSize)
        context.api
          .getLeaderboard(first, last)
          .map(scores => bson { "scores" :: scores })
          .onSuccess(bson => message.sendBson(HttpResponseStatus.OK, bson))
          .onFailure(message.sendException)
      )

    context.vertx
      .createHttpServer(httpOptions)
      .requestHandler(router)
      .listen(_ =>
        context.log.info(s"The server is up at ${httpOptions.getHost}:${httpOptions.getPort}")
      )
  end init

/** Companion object of [[StatisticsHttpAdapter]]. */
object StatisticsHttpAdapter:
  /**
   * The default number of users retrieved when querying the
   * leaderboard of this service.
   */
  private val DefaultLeaderboardWindowSize: Long = 100

  given Conversion[HttpResponseStatus, Int] = _.code()

  extension (self: RoutingContext) {

    /** Send a '200 OK' http response. */
    private def sendOk(): Unit =
      self.response.setStatusCode(HttpResponseStatus.OK).send()

    /**
     * Send an http response with the specified status code and the specified
     * [[BsonDocument]] as json content.
     *
     * @param statusCode the specified status code.
     * @param bson       the specified [[BsonDocument]].
     */
    private def sendBson(statusCode: Int, bson: BsonDocument): Unit =
      self.response
        .setStatusCode(statusCode)
        .putHeader("Content-Type", "application/json")
        .send(bsonToJson(bson).encode())

    /**
     * Send the specified [[Throwable]] as an http response
     * [[BsonDocument]] as json content.
     *
     * @param throwable the specified [[Throwable]].
     */
    private def sendException(throwable: Throwable): Unit =
      throwable.printStackTrace()
      self.sendBson(
        statusCode = throwable match {
          case _: MalformedInputException => HttpResponseStatus.BAD_REQUEST
          case _: UserNotFoundException   => HttpResponseStatus.NOT_FOUND
          case _: Throwable               => HttpResponseStatus.INTERNAL_SERVER_ERROR
        },
        bson = bson {
          "type" :: throwable.getClass.getSimpleName
          "message" :: throwable.getMessage
        }
      )

    /**
     * Get the value of specified path parameter if present.
     *
     * @param paramName the name of the specified path parameter.
     * @return the value of the specified path parameter.
     * @throws MalformedInputException if no value is bound to the specified path parameter.
     */
    private def requirePathParam(paramName: String): String =
      Option(self.pathParam(paramName)).getOrElse {
        throw MalformedInputException(s"Request missing path parameter '$paramName'.")
      }

    /**
     * Get the value of specified body parameter if present.
     *
     * @param path the path to the specified body parameter.
     * @return the value of the specified body parameter.
     * @throws MalformedInputException if no value is bound to the specified body parameter.
     */
    private def requireBodyParam(path: String): BsonValue =
      Try {
        jsonToBson(self.body.asJsonObject).require(path)
      }.getOrElse {
        throw MalformedInputException(s"Request missing body parameter '$path'.")
      }
  }
