package util.text

object LangUtils {

  def startsWithCyrillic(word: String): Boolean =
    word.headOption.exists(c =>
      'А' <= c && c <= 'Я' ||
        'а' <= c && c <= 'я' ||
        'Ё' == c ||
        'ё' == c
    )

}
