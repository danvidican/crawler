case class RequestBody(urls: List[String])
case class Response(result: List[Crawled], error: String)
case class Crawled(url: String, data: String)
