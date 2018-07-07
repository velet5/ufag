package bot

import telegram.Update

sealed trait Command

final case class Help(chatId: ChatId) extends Command
final case class Start(chatId: ChatId) extends Command
final case class Statistics(chatId: ChatId) extends Command
final case class Oxford(chatId: ChatId, word: String) extends Command
case object Unknown extends Command


trait UpdateParser[C <: Command] {
  def parse(update: Update): Option[C]
}

object Command {

  val helpParser: UpdateParser[Help] = simpleCommandParser("/help", Help)
  val startParser: UpdateParser[Start] = simpleCommandParser("/start", Start)
  val statisticsParser: UpdateParser[Statistics] = simpleCommandParser("/stat", Statistics)
  val oxfordParser: UpdateParser[Oxford] = requiredTextCommandParser("/ox", Oxford)

  val parsers = Seq(helpParser, startParser, statisticsParser, oxfordParser)

  def parse(update: Update): Command = {
    parsers.view
      .flatMap(_.parse(update))
      .headOption
      .getOrElse(Unknown)
  }


  private def simpleCommandParser[C <: Command](command: String, creator: ChatId => C): UpdateParser[C] =
    update =>
      for {
        chatId <- maybeChatId(update)
        c <- maybeBotCommand(update) if c.command == command
      } yield creator(chatId)

  private def requiredTextCommandParser[C <: Command](command: String, creator: (ChatId, String) => C): UpdateParser[C]  =
    update =>
      for {
        chatId <- maybeChatId(update)
        c <- maybeBotCommand(update) if c.command == command
        text <- c.text
      } yield creator(chatId, text)

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


  private case class BotCommand(command: String, text: Option[String])

}