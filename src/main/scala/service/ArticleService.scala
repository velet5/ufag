package service

import persistence.dao.ArticleDao
import persistence.model.Article
import persistence.model.Provider.Provider

import scala.concurrent.Future

class ArticleService(articleDao: ArticleDao) {

  def find(text: String, provider: Provider): Future[Option[Article]] = {
    articleDao.find(text, provider)
  }

  def save(searchText: String, content: String, provider: Provider): Future[Unit] = {
    articleDao.save(searchText, content, provider)
  }

}
