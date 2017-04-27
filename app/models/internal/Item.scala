package models.internal

import play.api.libs.json.Json

case class Item(
   by: String,
   id: Int,
   kids: Option[List[Int]],
   comment: Option[List[Comment]],
   parts: Option[List[Int]],
   score: Int,
   time: Long,
   title: String,
   url: Option[String],
   `type`: String
 )

object Item {

  def itemWithInflatedChildren(item: Item, list: List[CommentAndChildren]): Item = {
    Item(
      item.by,
      item.id,
      item.kids,
      Some(Comment.flatten(list)),
      item.parts,
      item.score,
      item.time,
      item.title,
      item.url,
      item.`type`
    )
  }

  implicit val jf = Json.format[Item]
}