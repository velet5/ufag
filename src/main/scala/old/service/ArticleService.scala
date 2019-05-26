package old.service

import old.persistence.Db
import old.persistence.dao.ArticleDao
import old.persistence.model.Article
import old.persistence.model.Provider.Provider

import scala.concurrent.{ExecutionContext, Future}

class ArticleService(
  db: Db,
  articleDao: ArticleDao
)(
  implicit ec: ExecutionContext
) {

  def find(text: String, provider: Provider): Future[Option[Article]] =
    db.run(articleDao.find(text, provider))

  def save(searchText: String, content: String, provider: Provider): Future[Int] =
    db.run(articleDao.save(Article(searchText, content, provider)))

}
