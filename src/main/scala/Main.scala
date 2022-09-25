import cats.Monad
import cats.effect.{ExitCode, IO, IOApp, Resource}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {


  override def run(args: List[String]): IO[ExitCode] = {

    def apis[F[_]: Monad](crawlService: CrawlService[F]) = Router(
      "/api/crawl" -> crawlService.crawlRoute,
    ).orNotFound

    val resource = for {
      browser <- Resource.eval(IO.delay(JsoupBrowser()))
      _ <- BlazeServerBuilder[IO](global)
           .bindHttp(8080, "localhost")
           .withHttpApp(apis(new CrawlService[IO](browser)))
           .resource
    } yield ()

    resource.use(_ => IO.never).as(ExitCode.Success)

  }

}
