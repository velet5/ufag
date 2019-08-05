package client

import cats.MonadError
import com.softwaremill.sttp.{MonadError => _, _}
import com.softwaremill.sttp.circe._
import conf.Configuration.OxfordProperties
import model.client.OxfordDefinition
import util.url.UrlUtils
import util.syntax.jsonResponseF._

trait OxfordClient[F[_]] {

  def define(word: String): F[OxfordDefinition]

}

object  OxfordClient {

  def create[F[_] : MonadError[?[_], Throwable]](
    properties: OxfordProperties,
  )(
    implicit sttpBackend: SttpBackend[F, Nothing],
  ): OxfordClient[F] =
    new Impl[F](properties)

  // internal

  private class Impl[F[_] : MonadError[?[_], Throwable]](
    properties: OxfordProperties
  )(
    implicit
    sttpBackend: SttpBackend[F, Nothing]
  ) extends OxfordClient[F] {

    override def define(word: String): F[OxfordDefinition] =
      sttp
        .get(uri"${properties.serviceUrl}/entries/en/${UrlUtils.encode(word)}")
        .header("app_id", properties.appId)
        .header("app_key", properties.apiKey)
        .response(asJson[OxfordDefinition])
        .send()
        .extract()
  }

}