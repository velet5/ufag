package velet5

import zio.*
import zio.interop.catz.*

object Application extends CatsApp {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = 
    

}
