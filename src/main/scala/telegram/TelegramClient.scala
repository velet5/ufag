package telegram

import cats.effect.Sync
import com.softwaremill.sttp.SttpBackend
import model.telegram.Update.ChatId

trait TelegramClient[F[_]] {
  def send(chatId: ChatId, message: String): F[Any]

  def forward(arg: Any): F[Any]

  def reply(arg: Any): F[Any]
}

object TelegramClient {

  def create[F[_] : Sync](
    sttpBackend: SttpBackend[F, Nothing]
  ): F[TelegramClient[F]] =
    Sync[F].delay(new Impl()(sttpBackend))

  class Impl[F[_]](
    implicit sttpBackend: SttpBackend[F, Nothing]
  ) extends TelegramClient[F] {
    override def send(chatId: ChatId, message: String): F[Any] = {
      ???
    }

    override def forward(arg: Any): F[Any] = ???

    override def reply(arg: Any): F[Any] = ???
  }

}
