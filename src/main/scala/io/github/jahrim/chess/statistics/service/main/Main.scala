package io.github.jahrim.chess.statistics.service.main

import com.mongodb.client.MongoClients
import com.mongodb.*
import org.bson.conversions.Bson
import org.bson.{BsonDocument, BsonInt64, Document}
import org.rogach.scallop.*

import scala.util.{Try, Using}

/** Main of the application. */
@main def main(args: String*): Unit =
  val arguments: Args = Args(args)

  val settings = MongoClientSettings
    .builder()
    .applyConnectionString(new ConnectionString(arguments.mongoDBConnection()))
    .serverApi(
      ServerApi
        .builder()
        .version(ServerApiVersion.V1)
        .build()
    )
    .build()

  Using(MongoClients.create(settings)) { client =>
    val db = Try(client.getDatabase(arguments.mongoDBDatabase()).getCollection("scores"))
    println(db.get.find(Document.parse(s"{ username: \"carlovinti\" }")).first().toJson)
  }

/**
 * The parsed command line arguments accepted by this application.
 *
 * @param arguments the sequence of command line arguments to parse.
 *
 * @see [[https://github.com/scallop/scallop Scallop Documentation on Github]].
 */
class Args(private val arguments: Seq[String]) extends ScallopConf(arguments):
  val mongoDBConnection: ScallopOption[String] = opt[String](
    name = "mongodb-connection",
    descr = "The connection string to the mongodb instance used by this service.",
    required = true
  )
  val mongoDBDatabase: ScallopOption[String] = opt[String](
    name = "mongodb-database",
    descr = "The database within the mongodb instance used by this service.",
    default = Some("statistics"),
    required = true
  )
  verify()
