package util.text

object RomanNumbers {

  private val romanTen = Vector("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")
  private val romans: Vector[String] = romanTen ++ romanTen.map("X" + _) ++ romanTen.map("XX" + _)

  def apply(x: Int): String = romans(x - 1)

}
