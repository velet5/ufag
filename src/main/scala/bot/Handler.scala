package bot

import model.bot.Command
import model.telegram.Update

class Handler[F[_], C <: Command](
  parser: Parser[C],
  action: Action[F, C],
) {

  def handle(update: Update): Option[F[Unit]] =
    parser
      .parse(update)
      .map(action.run)

}
