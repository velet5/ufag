import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import configuration.Configuration

trait Core {

  val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  val properties: Configuration = pureconfig.loadConfigOrThrow[Configuration]

}
