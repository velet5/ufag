package util.circe

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

trait CirceCodec[A] extends Encoder[A] with Decoder[A] {

  def imap[B](f: A => B, g: B => A): CirceCodec[B] =
    CirceCodec.of[B](this.map(f), this.contramap(g))

}

object CirceCodec {

  def of[A](decoder: Decoder[A], encoder: Encoder[A]): CirceCodec[A] =
    new CirceCodec[A] {
      override def apply(a: A): Json = encoder.apply(a)
      override def apply(c: HCursor): Result[A] = decoder.apply(c)
    }

  val codecLong: CirceCodec[Long] = of(Decoder.decodeLong, Encoder.encodeLong)

}
