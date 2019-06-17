package repository

import slick.lifted.{AbstractTable, TableQuery}

trait Repository[F[_], A <: AbstractTable[_]] {

  def tableQuery: TableQuery[A]

}
