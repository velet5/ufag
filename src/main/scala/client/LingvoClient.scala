package client

import java.nio.charset.StandardCharsets.UTF_8

import cats.effect.concurrent.Deferred
import cats.effect.{Concurrent, ContextShift}
import cats.instances.string._
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.show._
import cats.{Applicative, Show}
import client.LingvoClient.Lang
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.circe._
import com.softwaremill.sttp.{MonadError => _, _}
import conf.Configuration.LingvoProperties
import enumeratum.values.{StringEnum, StringEnumEntry}
import model.client.LingvoArticle
import monix.catnap.MVar
import util.syntax.JsonResponseError.NotOkError
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

  def create[F[_] : Concurrent : ContextShift](
    properties: LingvoProperties,
  )(
    implicit sttpBackend: SttpBackend[F, Nothing],
  ): F[LingvoClient[F]] =
    for {
      tokenVar <- MVar.empty[F, Deferred[F, Token]]()
    } yield new Impl(
      properties, tokenVar
    )

  sealed abstract class Lang(val value: String) extends StringEnumEntry

  object Lang extends StringEnum[Lang] {

    case object En extends Lang("1033")

    case object Ru extends Lang("1049")

    override val values: immutable.IndexedSeq[Lang] = findValues
  }

  // internal

  private class Impl[F[_] : Concurrent](
    properties: LingvoProperties,
    tokenVar: MVar[F, Deferred[F, Token]],
  )(
    implicit sttpBackend: SttpBackend[F, Nothing],
  ) extends LingvoClient[F] {

    override def translate(
      word: String,
      from: Lang,
      to: Lang
    ): F[List[LingvoArticle]] =
      authorized(token =>
        sttp
          .get(buildUri(word, from, to))
          .header(HeaderNames.Authorization, token.show)
          .response(asJson[List[LingvoArticle]])
          .send()
          .extractJson()
      )

    // internal

    private def authorized[A](fn: Token => F[A]): F[A] = {
      def go: F[A] =
        tokenVar.tryRead >>= {
          case Some(deffered) => deffered.get >>= fn
          case None => updateToken *> go
        }

      def retrieveToken =
        Deferred[F, Token].flatMap(deferred =>
          Concurrent[F]
            .start(authenticate >>= deferred.complete)
            .flatMap(_ => tokenVar.put(deferred))
        )

      def updateToken =
        tokenVar
          .tryTake
          .flatMap(opt => Applicative[F].whenA(opt.isEmpty)(retrieveToken))

      go.handleErrorWith {
        case NotOkError(401, _) => updateToken *> go
      }
    }

    private def buildUri(word: String, from: Lang, to: Lang): Uri =
      uri"${properties.serviceUrl}/api/v1/Translation"
        .copy(queryFragments = List(
          KeyValue("text", UrlUtils.encode(word)),
          KeyValue("srcLang", from.value),
          KeyValue("dstLang", to.value),
        ))

    private def authenticate(): F[Token] =
      sttp
        .post(uri"${properties.serviceUrl}/api/v1.1/authenticate")
        .header(HeaderNames.Authorization, show"Basic ${properties.apiKey}")
        .response(asString(UTF_8.toString))
        .send()
        .extract()
        .map(Token(_))

  }

  case class Token(value: String) extends AnyVal

  object Token {
    implicit val show: Show[Token] = Show(_.value)
  }

}
