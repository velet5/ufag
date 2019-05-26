package model.telegram

import io.circe.derivation.renaming.snakeCase
import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive

@derive(encoder(snakeCase), decoder(snakeCase))
case class Ok()
