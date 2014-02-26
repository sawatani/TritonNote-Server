package models.db

import java.util.Date
import scala.util.control.Exception._
import scalaz._
import Scalaz._
import com.amazonaws.services.dynamodbv2.model._
import models.GeoInfo

case class CatchReport(id: Long,
                       createdAt: Date,
                       lastModifiedAt: Option[Date],
                       timestamp: Date,
                       latitude: Double,
                       longitude: Double) {
  /**
   * Reload from DB.
   * If there is no longer me, returns None.
   */
  def refresh: Option[CatchReport] = CatchReports.get(id)
  /**
   * Delete me
   */
  def delete: Boolean = CatchReports.delete(id)
  /**
   * Point on map by latitude and longitude
   */
  lazy val geoinfo = GeoInfo(latitude, longitude)
  /**
   * All comments
   */
  lazy val comments: List[Comment] = Comments.find(Comments.catchReport(Option(this))).toList.sortBy {
    a => a.lastModifiedAt getOrElse a.createdAt
  }
  /**
   * Add comment
   */
  def addComment(text: String)(implicit user: User): Option[Comment] = {
    Comments.addNew(user, this, text)
  }
}

object CatchReports extends AutoIDTable[CatchReport]("CATCH_REPORT") {
  val timestamp = Column[Date]("TIMESTAMP", (_.timestamp), (_.getDate), attrDate)
  val latitude = Column[Double]("LATITUDE", (_.latitude), (_.getDouble), attrDouble)
  val longitude = Column[Double]("LONGITUDE", (_.longitude), (_.getDouble), attrDouble)
  // All columns
  val columns = Set(timestamp, latitude, longitude)
  def fromMap(implicit map: Map[String, AttributeValue]): Option[CatchReport] = allCatch opt CatchReport(
    id.build,
    createdAt.build,
    lastModifiedAt.build,
    timestamp.build,
    latitude.build,
    longitude.build
  )
  /**
   * Add new
   */
  def addNew(theGeoinfo: GeoInfo, theTimestamp: Date): Option[CatchReport] = addNew(
    timestamp(theTimestamp),
    latitude(theGeoinfo.latitude),
    longitude(theGeoinfo.longitude)
  )
}