import cats.Monad
import cats.effect.{ExitCode, IO, IOApp, Resource}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object Main extends IOApp {


  override def run(args: List[String]): IO[ExitCode] = {

    implicit val executionContext: ExecutionContextExecutorService =
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))

    def apis[F[_]: Monad](crawlService: CrawlService[F]) = Router(
      "/api/crawl" -> crawlService.crawlRoute,
    ).orNotFound

    val resource = for {
      browser <- Resource.eval(IO.delay(JsoupBrowser()))
      _ <- BlazeServerBuilder[IO](executionContext)
           .bindHttp(8080, "localhost")
           .withHttpApp(apis(new CrawlService[IO](browser)))
           .resource
    } yield ()

    resource.use(_ => IO.never).as(ExitCode.Success)

  }

}
