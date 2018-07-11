package util

import org.apache.commons.text.similarity.LevenshteinDistance

object WordSimilarity {

  private val levenshteinDistance = LevenshteinDistance.getDefaultInstance

  def calculate(one: String, two: String): Int =
    levenshteinDistance(one, two)

}
