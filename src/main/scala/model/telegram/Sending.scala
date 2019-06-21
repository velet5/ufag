package model.telegram

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import io.circe.generic.extras.ConfiguredJsonCodec
import model.telegram.Sending.ParseMode
import model.JsonConfig.snakeCase

import scala.collection.immutable

@ConfiguredJsonCodec
case class Sending(
  chatId: Chat.Id,
  text: String,
  parseMode: ParseMode,
)

object Sending {

  sealed abstract class ParseMode(val value: String) extends StringEnumEntry

  object ParseMode extends StringEnum[ParseMode] with StringCirceEnum[ParseMode] {

    case object Markdown extends ParseMode("Markdown")
    case object Html extends ParseMode("HTML")

    val values: immutable.IndexedSeq[ParseMode] = findValues
  }

}
