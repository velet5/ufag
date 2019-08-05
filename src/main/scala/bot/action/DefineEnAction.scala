package bot.action

import bot.Action
import cats.data.OptionT
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monad, ~>}
import client.{OxfordClient, OxfordFormatter, TelegramClient}
import model.bot.Command.DefineEn
import model.bot.Request
import model.repository.Article.Provider
import repository.ArticleRepository

class DefineEnAction[F[_] : Monad, DB[_]](
  articleRepository: ArticleRepository[DB],
  transact: DB ~> F,
  oxfordClient: OxfordClient[F],
  telegramClient: TelegramClient[F],
) extends Action[F, DefineEn] {

  override def run(request: Request[DefineEn]): F[Unit] =
    text(request.command)
      .flatMap(telegramClient.send(request.chatId, _))
      .void

  // internal

  private def text(command: DefineEn): F[String] =
    fromDb(command.text).getOrElseF(request(command.text))

  private def fromDb(text: String): OptionT[F, String] =
    OptionT
      .apply(
        transact(
          articleRepository.find(text, Provider.Oxford)
        )
      )
      .map(_.content)

  private def request(text: String): F[String] =
    oxfordClient
      .define(text)
      .map(OxfordFormatter.format)

}
