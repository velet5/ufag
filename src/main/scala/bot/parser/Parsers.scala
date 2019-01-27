package bot.parser

import bot.{ChatId, Command, Malformed, Unknown}
import org.apache.commons.lang3.StringUtils.{removeStart, startsWith}
import telegram.Update

object Parsers {

  private case class BotCommand(command: String, text: Option[String])

  lazy val unknownCommandParser: CommandParser[Unknown] =
    update =>
      update.message
        .filter(_.text.exists(_.startsWith("/")))
        .map(message => Right(Unknown(ChatId(message.chat.id))))

  def defaultParser[C <: Command](creator: (ChatId, String) => C): CommandParser[C] =
    update =>
      for {
        message <- update.message
        text <- message.text if !text.startsWith("/")
        chatId <- maybeChatId(update)
      } yield Right(creator(chatId, text.toLowerCase))

  def simpleCommandParser[C <: Command](command: String, creator: ChatId => C): CommandParser[C] =
    update =>
      for {
        chatId <- maybeChatId(update)
        c <- maybeBotCommand(update) if c.command == command
      } yield Right(creator(chatId))

  def withMessageId[C <: Command](command: String, creator: (ChatId, Long) => C): CommandParser[C] =
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

  def definitionParser[A](
    update: Update,
    filter: String => Boolean,
    constructor: (ChatId, String) => A): Option[Either[Malformed, A]] = {
    for {
      message <- update.message
      rawText <- message.text if startsWith(rawText, "?")
      text = removeStart(rawText, "?").trim() if filter(text)
      chatId <- maybeChatId(update)
    } yield {
      Right(constructor(chatId, text))
    }
  }

  // private

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
