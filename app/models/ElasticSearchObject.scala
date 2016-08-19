package models

import java.util.Date
import play.api.libs.json.Json._
import play.api.libs.json._


case class ElasticsearchTag (
  creator: String,
  created: Date,
  tag: String
)
object ElasticsearchTag {
  /**
    * Serializer for ElasticsearchTag
    */
  implicit object ElasticsearchTagWrites extends Writes[ElasticsearchTag] {
    def writes(est: ElasticsearchTag): JsValue = JsObject(Seq(
      "creator" -> JsString(est.creator),
      "created" -> JsString(est.created.toString),
      "tag" -> JsString(est.tag)
    ))
  }

  /**
    * Deserializer for ElasticsearchTag
    */
  implicit object ElasticsearchTagReads extends Reads[ElasticsearchTag] {
    def reads(json: JsValue): JsResult[ElasticsearchTag] = JsSuccess(new ElasticsearchTag(
      (json \ "creator").as[String],
      (json \ "created").as[Date],
      (json \ "tag").as[String]
    ))
  }
}

case class ElasticsearchComment (
  creator: String,
  created: Date,
  text: String
)
object ElasticsearchComment {
  /**
    * Serializer for ElasticsearchComment
    */
  implicit object ElasticsearchCommentWrites extends Writes[ElasticsearchComment] {
    def writes(esc: ElasticsearchComment): JsValue = JsObject(Seq(
      "creator" -> JsString(esc.creator),
      "created" -> JsString(esc.created.toString),
      "text" -> JsString(esc.text)
    ))
  }

  /**
    * Deserializer for ElasticsearchComment
    */
  implicit object ElasticsearchCommentReads extends Reads[ElasticsearchComment] {
    def reads(json: JsValue): JsResult[ElasticsearchComment] = JsSuccess(new ElasticsearchComment(
      (json \ "creator").as[String],
      (json \ "created").as[Date],
      (json \ "text").as[String]
    ))
  }
}

case class ElasticsearchObject (
  doctype: ResourceRef,
  creator: String,
  created: Date,
  parent_of: List[String] = List.empty,
  child_of: List[String] = List.empty,
  tags: List[ElasticsearchTag] = List.empty,
  comments: List[ElasticsearchComment] = List.empty, // TODO: are these actually used? might need to fetch like Metadata
  metadata: Map[String, JsValue] = Map()
)

object ElasticsearchObject {
  import ElasticsearchTag._
  import ElasticsearchComment._

  /**
    * Serializer for ElasticsearchObject
    */
  implicit object ElasticsearchWrites extends Writes[ElasticsearchObject] {
    def writes(eso: ElasticsearchObject): JsValue = Json.obj(
      "type" -> JsString(eso.doctype.toString),
      "creator" -> JsString(eso.creator),
      "created" -> JsString(eso.created.toString),
      "parent_of" -> JsArray(eso.parent_of.toSeq.map( (p:String) => Json.toJson(p)): Seq[JsValue]),
      "child_of" -> JsArray(eso.child_of.toSeq.map( (c:String) => Json.toJson(c)): Seq[JsValue]),
      "tags" -> JsArray(eso.tags.toSeq.map( (t:ElasticsearchTag) => Json.toJson(t)): Seq[JsValue]),
      "comments" -> JsArray(eso.comments.toSeq.map( (c:ElasticsearchComment) => Json.toJson(c)): Seq[JsValue]),
      "metadata" -> JsArray(eso.metadata.toSeq.map(
        (m:(String,JsValue)) => new JsObject(Seq(m._1 -> m._2)) )
      ) // TODO: Fetch metadata from MD collection rather than from dataset/file
    )
  }

  /**
    * Deserializer for ElasticsearchObject
    */
  implicit object ElasticsearchReads extends Reads[ElasticsearchObject] {
    def reads(json: JsValue): JsResult[ElasticsearchObject] = JsSuccess(new ElasticsearchObject(
      (json \ "type").as[ResourceRef],
      (json \ "creator").as[String],
      (json \ "created").as[Date],
      (json \ "parent_of").as[List[String]],
      (json \ "child_of").as[List[String]],
      (json \ "tags").as[List[ElasticsearchTag]],
      (json \ "comments").as[List[ElasticsearchComment]],
      (json \ "metadata").as[Map[String, JsValue]]
    ))
  }
}
