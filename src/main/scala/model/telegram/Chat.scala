package model.telegram

import io.circe.generic.extras.ConfiguredJsonCodec
import model.JsonConfig.snakeCase
import slick.lifted.Isomorphism
import util.circe.CirceCodec

@ConfiguredJsonCodec
case class Chat(id: Chat.Id)

object Chat {

  case class Id(value: Long)

  object Id {
    implicit val circeCodec: CirceCodec[Id] = CirceCodec.codecLong.imap(Id(_), _.value)
    implicit val isomorphism: Isomorphism[Id, Long] = new Isomorphism(_.value, Id(_))
  }

}
