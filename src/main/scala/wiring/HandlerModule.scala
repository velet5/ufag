package wiring

import bot.Handler
import cats.effect.Sync

case class HandlerModule[F[_]](
  handlers: Seq[Handler[F, _]]
)

object HandlerModule {

  def create[F[_]](implicit F: Sync[F]): F[HandlerModule[F]] =
    F.delay(HandlerModule(Seq.empty))

}