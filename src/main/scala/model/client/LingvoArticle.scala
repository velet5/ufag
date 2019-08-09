package model.client

import io.circe.generic.JsonCodec

@JsonCodec
case class LingvoArticle(
  title: String,
  titleMarkup: List[ArticleNode],
  dictionary: String,
  articleId: String,
  body: List[ArticleNode],
)

@JsonCodec
case class ArticleNode(
  text: String,
  `type`: Int,
  items: List[ArticleNode],
  isItalics: Boolean,
  isAccent: Boolean,
  node: String,
  fullText: String,
  fileName: String,
  dictionary: String,
  articleId: String,
  markup: List[ArticleNode],
  isOptional: Boolean
)
