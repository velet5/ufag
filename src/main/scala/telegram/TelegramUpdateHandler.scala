package telegram

import model.telegram.TelegramUpdate

class TelegramUpdateHandler[F[_]] {

  def handle(update: TelegramUpdate) = ???

}
