package controllers

import javax.inject._

import models.internal.{Comment, CommentAndChildren, Item}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HomeController @Inject() (ws: WSClient) extends Controller {

  val baseUrl = "https://hacker-news.firebaseio.com/v0"

  def topstories = Action.async { implicit request =>
    val response: Future[List[Item]] = for {
      topstories <-
        ws
          .url(s"${baseUrl}/topstories.json")
          .withHeaders(("Accept", "application/json"))
          .execute()

      listItems <- Future.sequence(topstories.json.as[List[Int]].map(v => {
        requestItem(v)
      }))
    } yield listItems.map(response => response.json.as[Item])


    response
      .map(s => Ok(Json.toJson(s)))
      .recover {case e => InternalServerError(e.getMessage)}
  }

  def item(id: Int) = Action.async {

    val future = for {
      itemResponse <- requestItem(id)
      item <- Future.successful(itemResponse.json.as[Item])
      commentFutures <- item.kids.map(getComments).getOrElse(Future.successful(List[CommentAndChildren]()))
    } yield Item.itemWithInflatedChildren(item, commentFutures)

    future
      .map(item => Ok(Json.toJson(item)))
      .recover {case e => InternalServerError(e.getMessage)}
  }

  private def requestItem(v: Int) = {
    ws.url(s"${baseUrl}/item/$v.json").execute()
  }

  private def getComments(commentIds: List[Int]) : Future[List[CommentAndChildren]] = {
    val comment = commentIds.map(id => for {
      commentRequest <- requestItem(id)
      comment <- Future.successful(commentRequest.json.as[Comment])
      children <- comment.kids.map(getComments).getOrElse(Future.successful(List()))
    } yield CommentAndChildren(comment, children))

    Future.sequence(comment)
  }

}
