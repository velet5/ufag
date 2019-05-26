package old.util

object TextUtils {

  def isCyrillic(text: String): Boolean =
    text.exists(cyrillic.indexOf(_) >= 0)

  // under the hood

  private val cyrillic =
    "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
    "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"

}
