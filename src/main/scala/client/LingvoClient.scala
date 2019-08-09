package client

import java.nio.charset.StandardCharsets.UTF_8

import cats.MonadError
import cats.instances.string._
import cats.syntax.show._
import client.LingvoClient.Lang
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.circe._
import com.softwaremill.sttp.{MonadError => _, _}
import conf.Configuration.LingvoProperties
import enumeratum.values.{StringEnum, StringEnumEntry}
import model.client.LingvoArticle
import util.syntax.jsonResponseF._
import util.syntax.responseF._
import util.url.UrlUtils

import scala.collection.immutable

trait LingvoClient[F[_]] {

  def translate(
    word: String,
    from: Lang,
    to: Lang
  ): F[List[LingvoArticle]]

}

object LingvoClient {

  def create[F[_] : MonadError[*[_], Throwable]](
    properties: LingvoProperties,
  )(
    implicit sttpBackend: SttpBackend[F, Nothing],
  ): LingvoClient[F] =
    new Impl(properties)

  sealed abstract class Lang(val value: String) extends StringEnumEntry

  object Lang extends StringEnum[Lang] {

    case object En extends Lang("1033")

    case object Ru extends Lang("1049")

    override val values: immutable.IndexedSeq[Lang] = findValues
  }

  // internal

  private class Impl[F[_] : MonadError[*[_], Throwable]](
    properties: LingvoProperties
  )(
    implicit sttpBackend: SttpBackend[F, Nothing],
  ) extends LingvoClient[F] {

    override def translate(
      word: String,
      from: Lang,
      to: Lang
    ): F[List[LingvoArticle]] =
      sttp
        .get(buildUri(word, from, to))
        .header(???, ???)
        .response(asJson[List[LingvoArticle]])
        .send()
        .extractJson()

    // internal

    private def buildUri(word: String, from: Lang, to: Lang): Uri =
      uri"${properties.serviceUrl}/api/v1/Translation"
        .copy(queryFragments = List(
          KeyValue("text", UrlUtils.encode(word)),
          KeyValue("srcLang", from.value),
          KeyValue("dstLang", to.value),
        ))

    private def authenticate(): F[String] =
      sttp
        .post(uri"${properties.serviceUrl}/api/v1.1/authenticate")
        .header(HeaderNames.Authorization, show"Basic ${properties.apiKey}")
        .response(asString(UTF_8.toString))
        .send()
        .extract()

  }

}
