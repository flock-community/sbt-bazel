
package company

import zio._

object Main extends App {
   def run(args: List[String]) = Service.sayHello.exitCode
}
