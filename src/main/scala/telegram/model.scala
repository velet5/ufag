package telegram

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(classOf[SnakeCaseStrategy])
@JsonIgnoreProperties(ignoreUnknown = true)
case class Chat(lastName: String,
                id: Long,
                firstName: String,
                username: String)


@JsonNaming(classOf[SnakeCaseStrategy])
@JsonIgnoreProperties(ignoreUnknown = true)
case class Message(date: Long,
                   chat: Chat,
                   messageId: Long,
                   from: Chat,
                   text: Option[String],
                   replyToMessage: Option[Message],
                   entities: Option[Seq[MessageEntity]],
                   forwardFrom: Option[User])

@JsonNaming(classOf[SnakeCaseStrategy])
@JsonIgnoreProperties(ignoreUnknown = true)
case class User(id: Long)

@JsonNaming(classOf[SnakeCaseStrategy])
@JsonIgnoreProperties(ignoreUnknown = true)
case class Update(updateId: Long,
                  message: Option[Message])


@JsonNaming(classOf[SnakeCaseStrategy])
case class TelegramSendMessage(chatId: Long,
                       text: String,
                       parseMode: Option[String] = Some("Markdown"),
                       replyToMessageId: Option[Long] = None)

@JsonNaming(classOf[SnakeCaseStrategy])
case class ForwardMessage(chatId: Long,
                          fromChatId: Long,
                          messageId: Long,
                          disableNotification: Boolean = true)

@JsonNaming(classOf[SnakeCaseStrategy])
@JsonIgnoreProperties(ignoreUnknown = true)
case class TelegramResult(messageId: Long)


@JsonNaming(classOf[SnakeCaseStrategy])
@JsonIgnoreProperties(ignoreUnknown = true)
case class MessageResult(messageId: Long)


@JsonIgnoreProperties(ignoreUnknown = true)
case class TelegramResponse(ok: Boolean, result: MessageResult)


@JsonNaming(classOf[SnakeCaseStrategy])
@JsonIgnoreProperties(ignoreUnknown = true)
case class MessageEntity(`type`: String)