
package company

import zio.test._
import zio.test.environment._

object ServiceTest extends DefaultRunnableSpec {
   def spec = suite("HelloWorldSpec")(
    testM("sayHello correctly displays output") {
      for {
        _      <- Service.sayHello
        output <- TestConsole.output
      } yield assert(output)(Assertion.equalTo(Vector("Hello world\n")))
    }
  )
}
