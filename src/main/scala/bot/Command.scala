package bot

import bot.parser.AskReplyParser
import telegram.Update

sealed trait Command

final case class Help(chatId: ChatId) extends Command
final case class Start(chatId: ChatId) extends Command
final case class Statistics(chatId: ChatId) extends Command
final case class Oxford(chatId: ChatId, word: String) extends Command
final case class Lingvo(chatId: ChatId, word: String) extends Command
final case class Ask(chatId: ChatId, messageId: Long) extends Command
final case class AskReply(userId: Long, replyMessageId: Long, text: String) extends Command
final case class RuDefine(chatId: ChatId, word: String) extends Command
final case class Malformed(chatId: ChatId, text: String) extends Command
final case class Unknown(chatId: ChatId) extends Command
final case class Subscribe(chatId: ChatId) extends Command
final case class Unsubscribe(chatId: ChatId) extends Command
case object CannotParse extends Command


trait UpdateParser[C <: Command] {
  def parse(update: Update): Option[Either[Malformed, C]]
}

object Command {

  private case class BotCommand(command: String, text: Option[String])

  val parsers: Seq[UpdateParser[_ <: Command]] = Seq(
    defaultParser(Lingvo),
    requiredTextCommandParser("/ox", Oxford),
    requiredTextCommandParser("/ru", RuDefine),
    simpleCommandParser("/help", Help),
    simpleCommandParser("/start", Start),
    simpleCommandParser("/stat", Statistics),
    withMessageId("/ask", Ask),
    new AskReplyParser,
    simpleCommandParser("/sub", Subscribe),
    simpleCommandParser("/unsub", Unsubscribe)
  )

  def parse(update: Update): Command = {
    val option: Option[Either[Malformed, Command]] = parsers.view.flatMap(_.parse(update)).headOption

    option
      .map(_.fold(identity, identity))
      .orElse(unknown(update))
      .getOrElse(CannotParse)
  }

  def unknown(update: Update): Option[Unknown] =
    maybeChatId(update).map(Unknown)

  private def defaultParser[C <: Command](creator: (ChatId, String) => C): UpdateParser[C] =
    update =>
      for {
        message <- update.message
        text <- message.text if !text.startsWith("/")
        chatId <- maybeChatId(update)
      } yield Right(creator(chatId, text.toLowerCase))

  private def simpleCommandParser[C <: Command](command: String, creator: ChatId => C): UpdateParser[C] =
    update =>
      for {
        chatId <- maybeChatId(update)
        c <- maybeBotCommand(update) if c.command == command
      } yield Right(creator(chatId))

  private def requiredTextCommandParser[C <: Command](command: String, creator: (ChatId, String) => C): UpdateParser[C]  =
    update =>
      for {
        chatId <- maybeChatId(update)
        c <- maybeBotCommand(update) if c.command == command
      } yield c.text match {
        case Some(text) => Right(creator(chatId, text.toLowerCase))
        case None => Left(Malformed(chatId, s"Команда `$command` требует дополнительный текст, напишите что нибудь после `$command`"))
      }

  private def withMessageId[C <: Command](command: String, creator: (ChatId, Long) => C): UpdateParser[C] =
    update =>
      for {
        chatId <- maybeChatId(update)
        c <- maybeBotCommand(update) if c.command == command
        message <- update.message
        messageId = message.messageId
      } yield c.text match {
        case Some(_) => Right(creator(chatId, messageId))
        case None => Left(Malformed(chatId, s"Команда `$command` требует дополнительный текст, напишите что нибудь после `$command`"))
      }

  private def maybeChatId(update: Update): Option[ChatId] = update.message.map(_.chat.id).map(ChatId)

  private def maybeBotCommand(update: Update): Option[BotCommand] = {
    def fromText(text: String): BotCommand = {
      val spaceIndex = text.indexOf(' ')
      val splitIndex = if (spaceIndex == -1) text.length else spaceIndex
      val (command, argumentRaw) = text.splitAt(splitIndex)
      val argument = argumentRaw.trim

      BotCommand(
        command,
        text = if (argument.isEmpty) None else Some(argument))
    }

    for {
      message <- update.message if message.entities.exists(_.exists(_.`type` == "bot_command"))
      text <- message.text
    } yield fromText(text)
  }

}