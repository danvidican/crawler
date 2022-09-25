import cats.effect.{Concurrent, IO}
import cats.effect.unsafe.implicits.global
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.scalatest.Assertions
import org.scalatest.funsuite.AsyncFunSuiteLike

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class CrawlerTest extends AsyncFunSuiteLike {

  private def createCrawler: IO[CrawlService[IO]] = {
    IO.delay(JsoupBrowser()).map(b => new CrawlService[IO](b))
  }

  private def effectTimeout: FiniteDuration = 30.seconds

  test("return an error message in response for invalid URL") {
    val effect = createCrawler.flatMap(crawler => crawler.startCrawling(
      RequestBody(List("invalidUrl"))
    ))

    val result = effect.map(r =>
      Assertions.assert(r == Response(Nil, "Malformed URL: invalidUrl"))
    )
    Concurrent[IO].timeout(result, effectTimeout).unsafeToFuture()
  }

  test("return a correct response for a valid URL") {
    val effect = createCrawler.flatMap(crawler => crawler.startCrawling(
      RequestBody(List("https://www.google.com"))
    ))

    val result = effect.map(r =>
      Assertions.assert(r == Response(List(Crawled("https://www.google.com", "© 2022 - Confidențialitate - Termeni")), ""))
    )
    Concurrent[IO].timeout(result, effectTimeout).unsafeToFuture()
  }

}
