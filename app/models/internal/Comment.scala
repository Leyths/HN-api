package models.internal

import play.api.libs.json._
import play.api.libs.functional.syntax._


case class Comment(
                 by: Option[String],
                 id: Int,
                 kids: Option[List[Int]],
                 children: Option[List[Comment]],
                 time: Long,
                 parent: Int,
                 text: Option[String],
                 `type`: String
               )

case class CommentAndChildren(comment: Comment, children: List[CommentAndChildren])

object Comment {

  def flatten(list: List[CommentAndChildren]): List[Comment] = {
    list.map(c => Comment(
      c.comment.by,
      c.comment.id,
      c.comment.kids,
      Some(flatten(c.children)),
      c.comment.time,
      c.comment.parent,
      c.comment.text,
      c.comment.`type`
    ))
  }

  implicit object CommentFormat extends Format[Comment] {
    def reads(json: JsValue) = JsSuccess(Comment(
      (json \ "by").asOpt[String],
      (json \ "id").as[Int],
      (json \ "kids").asOpt[List[Int]],
      (json \ "children").asOpt[List[Comment]],
      (json \ "time").as[Long],
      (json \ "parent").as[Int],
      (json \ "text").asOpt[String],
      (json \ "type").as[String]
    ))
    def writes(u: Comment): JsValue = Json.obj(
      "by" -> u.by,
      "time" -> u.time,
      "text" -> u.text,
      "children" -> u.children
    )
  }
}