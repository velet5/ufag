package persistence.model

object Provider extends Enumeration {
  type Provider = Value

  val Lingvo: Provider.Value = Value(1)
  val Oxford: Provider.Value = Value(2)
  val LingvoRu: Provider.Value = Value(3)
}