package bot

import model.bot.{Command, Request}

trait Action[F[_], C <: Command] {

  def run(request: Request[C]): F[Unit]

}
