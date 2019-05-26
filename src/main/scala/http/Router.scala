package http

trait Router[F[_]] {

  implicit def toFuture()

}
