package util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object UrlUtils {

  def encode(string: String): String =
    URLEncoder
      .encode(string.toLowerCase, StandardCharsets.UTF_8.name())
      .replace("+", "%20")

}
