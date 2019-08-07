package bot

import model.bot.{Command, Request}

trait Action[F[_], C <: Command] {

  type Req = Request[C]

  def run(request: Req): F[Unit]

}
