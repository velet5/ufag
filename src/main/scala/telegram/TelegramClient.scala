package telegram

class TelegramClient[F[_]] {

  def send(message: Any): F[Any] = ???

  def forward(arg: Any): F[Any] = ???

  def reply(arg: Any): F[Any] = ???

}
