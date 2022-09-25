import cats.Parallel
import cats.effect.Async
import cats.effect.kernel.{Concurrent, Sync}
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.texts
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

class CrawlService[F[_]: Concurrent : Async: Parallel]  (browser: Browser) {

  def crawlRoute: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    implicit val requestDecoder: EntityDecoder[F, RequestBody] = jsonOf[F, RequestBody]
    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        (for {
          reqBody <- req.as[RequestBody]
          result <- startCrawling(reqBody)
        } yield result).flatMap(r => Ok(r.asJson))
    }
  }

  // parSequence is used here from Parallel typeclass to ensure parallelism.
  def startCrawling(body: RequestBody): F[Response] = {
    body.urls.map(crawlUrl).parSequence.map(_.partitionMap(identity))
      .map { case (errors, results) => Response(results, errors.mkString(" \n ")) }
  }

  /**
    This method extracts  all the texts from "p"
    If you want to improve this scraper, the documentation for library that I used can be found here:
    https://github.com/ruippeixotog/scala-scraper#quick-start
   */
  def crawlUrl(url: String): F[Either[String, Crawled]] =
    Sync[F].delay(browser.get(url)).flatMap { doc => {
      Sync[F].delay(doc >> texts("p")).map(c => {
        Right[String, Crawled](Crawled(url, c.toList.mkString(", "))).withLeft[String]
      }
      )
    }
    }.handleError(e => {
      val errorMessage = if(e.getMessage.isEmpty) e.toString else e.getMessage
      Left[String, Crawled](errorMessage).withRight[Crawled]
    })

}
