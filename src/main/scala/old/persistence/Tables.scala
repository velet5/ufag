package old.persistence

import old.persistence.model.{ArticleTable, AskingTable, QueryTable}
import slick.lifted.TableQuery

object Tables {

  val articles = TableQuery[ArticleTable]
  val askings = TableQuery[AskingTable]
  val queries = TableQuery[QueryTable]

}
