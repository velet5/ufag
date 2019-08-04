package bot

import cats.effect.Sync
import model.bot.Command
import model.telegram.Update

trait Handler[F[_], C <: Command] {
  def handle(update: Update): Option[F[Unit]]
}

object Handler {

  def create[F[_] : Sync, C <: Command](
    parser: Parser[C],
    action: Action[F, C],
  ): Handler[F, C] =
    parser.parse(_).map(action.run)

}
