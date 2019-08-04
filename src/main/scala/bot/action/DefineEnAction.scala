package bot.action

import bot.Action
import model.bot.Command.DefineEn
import model.bot.Request

class DefineEnAction[F[_]] extends Action[F, DefineEn] {

  override def run(request: Request[DefineEn]): F[Unit] = ???

}
