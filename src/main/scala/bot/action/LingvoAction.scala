package bot.action

import java.time.ZonedDateTime

import bot.Action
import cats.data.OptionT
import cats.effect.Concurrent
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monad, ~>}
import client.LingvoClient.Lang
import client.{LingvoClient, LingvoFormatter, TelegramClient}
import model.bot.Command.TranslateEn
import model.client.LingvoArticle
import model.repository.Article.Provider
import model.repository.Query
import model.telegram.Chat
import mouse.anyf._
import repository.{ArticleRepository, QueryRepository}
import util.{Clock, Memory}

class LingvoAction[F[_] : Concurrent, Db[_]](
  articleRepository: ArticleRepository[Db],
  queryRepository: QueryRepository[Db],
  telegramClient: TelegramClient[F],
  lingvoClient: LingvoClient[F],
  transact: Db ~> F,
  clock: Clock[F],
  from: Lang,
  to: Lang,
  provider: Provider,
) extends Action[F, TranslateEn] {

  override def run(request: Req): F[Unit] =
    maybeLastMonth(request)
      .semiflatMap(warn(request.chatId, _))
      .getOrElseF(translate(request))

  // internal

  private def translate(req: Req): F[Unit] =
    getDefinition(req.command.text)
      .flatMap(string => telegramClient.send(req.chatId, string))
      .flatMap(message =>
        saveQuery(
          Query(req.chatId, req.command.text, _, message.messageId)
        )
      )

  private def warn(chatId: Chat.Id, query: Query): F[Unit] = {
    val send = telegramClient.send(chatId, Memory.fag(query.time))
    val save = saveQuery(now => query.copy(time = now))

    send *> save
  }

  private def saveQuery(fn: ZonedDateTime => Query): F[Unit] =
    clock.zonedDateTime >>= { now =>
      (queryRepository.save(fn(now)) ||> transact).void
    }

  private def maybeLastMonth(request: Req): OptionT[F, Query] =
    OptionT(
      queryRepository.find(request.chatId, request.command.text) ||> transact
    )

  private def getDefinition(text: String): F[String] =
    fromCache(text).getOrElseF(query(text))

  private def query(text: String): F[String] =
    for {
      articles <- lingvoClient.translate(text, from, to)
      _ <- Concurrent[F].start(save(text, articles))
    } yield LingvoFormatter.format(articles)

  private def save(word: String, article: List[LingvoArticle]): F[Unit] =
    transact(
      articleRepository.save(word, article, provider)
    ).void

  private def fromCache(text: String): OptionT[F, String] =
    OptionT(
      articleRepository.find(text, Provider.Lingvo) ||> transact
    ).map(_.content)

}
