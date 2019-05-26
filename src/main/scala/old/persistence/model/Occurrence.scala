package old.persistence.model

import java.time.ZonedDateTime

case class Occurrence(text: String, when: ZonedDateTime, messageId: Long)
