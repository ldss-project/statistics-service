package io.github.jahrim.chess.statistics.service.components.adapters.http

import io.github.jahrim.chess.statistics.service.components.adapters.http.StatisticsHttpAdapter.DefaultLeaderboardWindowSize
import io.github.jahrim.chess.statistics.service.util.extensions.RoutingContextExtension.*
import io.github.jahrim.chess.statistics.service.util.extensions.JsonObjectExtension.*
import io.github.jahrim.chess.statistics.service.util.extensions.VertxFutureExtension.future
import io.github.jahrim.chess.statistics.service.components.exceptions.UserNotFoundException
import io.github.jahrim.chess.statistics.service.components.data.codecs.Codecs.given
import io.github.jahrim.chess.statistics.service.components.adapters.http.handlers.LogHandler
import io.github.jahrim.chess.statistics.service.components.ports.StatisticsPort
import io.github.jahrim.hexarc.persistence.bson.dsl.BsonDSL.{*, given}
import io.github.jahrim.hexarc.architecture.vertx.core.components.{Adapter, AdapterContext}
import io.vertx.ext.web.{Router, RoutingContext}
import io.vertx.ext.web.handler.{BodyHandler, CorsHandler}
import io.vertx.core.http.{HttpServerOptions, HttpServerResponse}
import io.vertx.core.Handler
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.util.Try

/** An [[Adapter]] that enables http communication with a [[StatisticsPort]] of a service. */
class StatisticsHttpAdapter(
    httpOptions: HttpServerOptions = HttpServerOptions().setHost("localhost").setPort(8080),
    allowedOrigins: Seq[String] = Seq()
) extends Adapter[StatisticsPort]:

  override protected def init(context: AdapterContext[StatisticsPort]): Unit =
    val router: Router = Router.router(context.vertx)

    router
      .route()
      .handler(CorsHandler.create().addOrigins(allowedOrigins.asJava))
      .handler(BodyHandler.create())
      .handler(LogHandler(context.log.info))

    router
      .get("/")
      .handler(_.response.send("Welcome to the Statistics Service!"))

    router
      .post("/score/:username")
      .handler(message =>
        future {
          val username: String = message.requirePathParam("username")
          val hasWon: Boolean = message.requireBodyParam("score.hasWon").as[Boolean]
          (username, hasWon)
        }
          .compose(context.api.addScore(_, _))
          .onSuccess(ok(message))
          .onFailure(fail(message))
      )

    router
      .delete("/score/:username")
      .handler(message =>
        future(message.requirePathParam("username"))
          .compose(context.api.deleteScores(_))
          .onSuccess(ok(message))
          .onFailure(fail(message))
      )

    router
      .get("/score/:username")
      .handler(message =>
        future(message.requirePathParam("username"))
          .compose(context.api.getScore(_))
          .map(score => bson { "score" :: score }.toJson)
          .onSuccess(json => message.sendJson(json))
          .onFailure(fail(message))
      )

    router
      .get("/score/history/:username")
      .handler(message =>
        future(message.requirePathParam("username"))
          .compose(context.api.getScoreHistory(_))
          .map(scores => bson { "scores" :: scores }.toJson)
          .onSuccess(json => message.sendJson(json))
          .onFailure(fail(message))
      )

    router
      .get("/leaderboard")
      .handler(message =>
        future {
          val first: Long =
            Try(message.queryParam("first").get(0))
              .map(_.toLong)
              .getOrElse(1)
          val last: Long =
            Try(message.queryParam("last").get(0))
              .map(_.toLong)
              .getOrElse(first + DefaultLeaderboardWindowSize)
          (first, last)
        }
          .compose(context.api.getLeaderboard(_, _))
          .map(scores => bson { "scores" :: scores }.toJson)
          .onSuccess(json => message.sendJson(json))
          .onFailure(fail(message))
      )

    context.vertx
      .createHttpServer(httpOptions)
      .requestHandler(router)
      .listen(_ => context.log.info("The server is up!"))

  end init

  /** Default success handler for a message received by the [[StatisticsHttpAdapter]]. */
  private def ok[T](message: RoutingContext): Handler[T] = _ => message.ok.send()

  /** Default failure handler for a message received by the [[StatisticsHttpAdapter]]. */
  private def fail(message: RoutingContext): Handler[Throwable] = e =>
    val response: HttpServerResponse = e match
      case e: IllegalArgumentException => message.error(400, e)
      case e: UserNotFoundException    => message.error(404, e)
      case e: Exception                => e.printStackTrace(); message.error(500, e)
    response.send()

/** Companion object of [[StatisticsHttpAdapter]]. */
object StatisticsHttpAdapter:
  /**
   * The default number of users retrieved when querying the
   * leaderboard of this service.
   */
  private val DefaultLeaderboardWindowSize: Long = 100
