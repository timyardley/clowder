package services

import play.api.{ Plugin, Logger, Application, Configuration }
import com.typesafe.config.ConfigFactory
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Channel
import play.libs.Akka
import akka.actor.Props
import akka.actor.Actor
import com.mongodb.casbah.MongoURI
import com.mongodb.casbah.MongoConnection
import play.api.Mode
import com.mongodb.MongoException
import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.gridfs.GridFS

/**
 * Mongo Salat service.
 *
 * @author Rob Kooper
 *
 */
class MongoSalatPlugin(app: Application) extends Plugin {
  case class MongoSource(
    uri: MongoURI
  ) {
    def db: MongoDB = uri.connectDB.fold(l => throw(l), r => r)

    def collection(name: String): MongoCollection = db(name)
  }

  object MongoSource {
    lazy val conn = MongoConnection()
  }

  lazy val configuration = app.configuration.getConfig("mongodb").getOrElse(Configuration.empty)
  lazy val sources: Map[String, MongoSource] = configuration.subKeys.map { sourceKey =>
    val uri = configuration.getString(sourceKey)
    val source = uri match {
      case None => null
      case Some(x) => MongoSource(MongoURI(x))
    }
    sourceKey -> source
  }.toMap

  override def onStart() {
    Logger.debug("Starting up MongoSalat plugin.")

    app.mode match {
      case Mode.Test =>
      case _ => {
        sources.map { source =>
          try {
            source._2.db.collectionNames
          } catch {
            case e: MongoException => throw configuration.reportError("mongodb." + source._1, "couldn't connect to [" + source._2.uri + "]", Some(e))
          } finally {
            Logger("play").info("mongodb [" + source._1 + "] connected at " + source._2)
          }
        }
      }
    }
  }

  override def onStop() {
    Logger.debug("Shutting down MongoSalat plugin.")
  }

  override def enabled = !configuration.subKeys.isEmpty
  
  /**
   * Returns the MongoSource that has been configured in application.conf
   * @param source The source name ex. default
   * @return A MongoSource
   */
  def source(source: String): MongoSource = {
    sources.get(source).getOrElse(throw configuration.reportError("mongodb." + source, source + " doesn't exist"))
  }
 
  /**
   * Returns MongoDB for configured source
   * @param sourceName The source name ex. default
   * @return A MongoDB
   */
  def db(sourceName:String = "default"): MongoDB = source(sourceName).db

  /**
   * Returns MongoCollection that has been configured in application.conf
   * @param collectionName The MongoDB collection name
   * @param sourceName The source name ex. default
   * @return A MongoCollection
   */
  def collection(collectionName: String, sourceName: String = "default"): MongoCollection = source(sourceName).collection(collectionName)
  
  
  /**
   * Returns GridFS for configured source
   * @param bucketName The bucketName for the GridFS instance
   * @param sourceName The source name ex. default
   * @return A GridFS
   */
  def gridFS(bucketName: String = "fs", sourceName:String = "default"): GridFS = GridFS(source(sourceName).db, bucketName)

}