import bot._
import bot.parser.{AskReplyParser, CommandParser}
import org.apache.commons.lang3.StringUtils.{removeStart, startsWith}
import telegram.Update
import util.TextUtils.isCyrillic

trait Parsers { this: Core =>

  val ruDefineParser: CommandParser[RuDefine] =
    definitionParser(_)
      .filter { case (_, text) => isCyrillic(text) }
      .map(RuDefine.tupled)
      .map(Right(_))

  val enDefineParser: CommandParser[Oxford] =
    definitionParser(_)
      .filter { case (_, text) => !isCyrillic(text) }
      .map(Oxford.tupled)
      .map(Right(_))

  val translationParser: CommandParser[Lingvo] = defaultParser(Lingvo)
  val startParser: CommandParser[Start] = simpleCommandParser("/start", Start)
  val helpParser: CommandParser[Help] = simpleCommandParser("/help", Help)
  val statParser: CommandParser[Statistics] = simpleCommandParser("/stat", Statistics)
  val askParser: CommandParser[Ask] = withMessageId("/ask", Ask)
  val askReplyParser: CommandParser[AskReply] = new AskReplyParser(properties.ufag)

  // private

  private case class BotCommand(command: String, text: Option[String])

  private def defaultParser[C <: Command](creator: (ChatId, String) => C): CommandParser[C] =
    update =>
      for {
        message <- update.message
        text <- message.text if !text.startsWith("/")
        chatId <- maybeChatId(update)
      } yield Right(creator(chatId, text.toLowerCase))

  private def simpleCommandParser[C <: Command](command: String, creator: ChatId => C): CommandParser[C] =
    update =>
      for {
        chatId <- maybeChatId(update)
        c <- maybeBotCommand(update) if c.command == command
      } yield Right(creator(chatId))

  private lazy val definitionParser: Update => Option[(ChatId, String)] = {
    update =>
      for {
        message <- update.message
        rawText <- message.text if startsWith(rawText, "?")
        text = removeStart(rawText, "?").trim() if isCyrillic(text)
        chatId <- maybeChatId(update)
      } yield {
        (chatId, text)
      }
  }

  private def maybeChatId(update: Update): Option[ChatId] = update.message.map(_.chat.id).map(ChatId)

  private def withMessageId[C <: Command](command: String, creator: (ChatId, Long) => C): CommandParser[C] =
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
