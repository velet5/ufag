package bot.action

import java.time.ZonedDateTime

import bot.Action
import cats.data.OptionT
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monad, ~>}
import client.TelegramClient
import model.bot.Command.TranslateEn
import model.repository.Article.Provider
import model.repository.Query
import model.telegram.Chat
import mouse.anyf._
import repository.{ArticleRepository, QueryRepository}
import util.{Clock, Memory}

class TranslateEnAction[F[_] : Monad, Db[_]](
  articleRepository: ArticleRepository[Db],
  queryRepository: QueryRepository[Db],
  telegramClient: TelegramClient[F],
  transact: Db ~> F,
  clock: Clock[F],
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
    ???

  private def fromCache(text: String): OptionT[F, String] =
    OptionT(
      articleRepository.find(text, Provider.Lingvo) ||> transact
    ).map(_.content)

}
