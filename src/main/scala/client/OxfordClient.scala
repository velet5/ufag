package client

import model.client.OxfordDefinition

trait OxfordClient[F[_]] {

  def define(word: String): F[OxfordDefinition]

}

object  OxfordClient {

  def create[F[_]](): OxfordClient[F] = ???

  // internal

  private class Impl[F[_]] extends OxfordClient[F] {
    override def define(word: String): F[OxfordDefinition] = ???
  }

}