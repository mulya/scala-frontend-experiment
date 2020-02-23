package ru.mulya.scala.frontend.experiment.http

import akka.actor.ActorSystem
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{Cache, CachingSettings}
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server._
import akka.stream.Materializer
import ru.mulya.scala.frontend.experiment.db.Models
import ru.mulya.scala.frontend.experiment.html.Templates
import ru.mulya.scala.frontend.experiment.service.TagServiceLike
import org.slf4j.LoggerFactory
import akka.http.scaladsl.server.directives.CachingDirectives._
import ru.mulya.scala.frontend.experiment.db.Models.Tag
import ru.mulya.scala.frontend.experiment.html.Templates
import ru.mulya.scala.frontend.experiment.json.JsonProtocols
import ru.mulya.scala.frontend.experiment.service.TagServiceLike

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class Server(tagService: TagServiceLike)(implicit sys: ActorSystem, ec: ExecutionContext, mat: Materializer)
  extends HttpApp with JsonProtocols {

  final val WEBJAR_PREFIX_PATH = "META-INF/resources/webjars"
  final val LOCAL_PREFIX_PATH = "assets"

  private val logger = LoggerFactory.getLogger(getClass)

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  // Use the request's URI as the cache's key
  val cacheKeyerFunction: PartialFunction[RequestContext, Uri.Path] = {
    case r: RequestContext => r.request.uri.path
  }
  private[this] val defaultCachingSettings = CachingSettings(sys)
  private[this] val lfuCacheSettings =
    defaultCachingSettings.lfuCacheSettings
      .withInitialCapacity(1000)
      .withMaxCapacity(10000)
      .withTimeToLive(1.minute)
      .withTimeToIdle(30.seconds)
  private[this] val cachingSettings =
    defaultCachingSettings.withLfuCacheSettings(lfuCacheSettings)
  private[this] val lfuCache: Cache[Uri.Path, RouteResult] = LfuCache(cachingSettings)

  object IdsSegment extends PathMatcher1[Set[Long]] {
    def apply(path: Path) =
      Segment.apply(path)
        .map { t =>
          val res = t._1.split(";").map(_.toLong).toSet
          Tuple1(res)
        }
  }

  private[this] def completeOrElse(f1: Route, f2: Route): Route = {
    x: RequestContext =>
      f1(x).flatMap {
        case c: Complete =>
          Future.successful(c)
        case _: Rejected =>
          f2(x)
      }
  }

  def pagesRoute: Route =
    pathSingleSlash {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Templates.tracksPage.toString()))
    } ~
    path("tags") {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Templates.tagsPage.toString()))
    }

  def assetsRoute: Route =
    pathPrefix("assets") {
      extractUnmatchedPath { assetPath =>
        logger.debug(s"Fetching asset: $assetPath")
        completeOrElse(
          getFromResource(LOCAL_PREFIX_PATH + assetPath),
//          getFromResource(WEBJAR_PREFIX_PATH + assetPath)
          getFromResourceDirectory("")
        )
      }
    }

  val postAndDeleteResponseTransformer: Boolean => HttpResponse = {
    case false =>
      HttpResponse(status = StatusCodes.NotFound)
    case true =>
      HttpResponse(status = StatusCodes.OK)
  }

  def tagsRoute: Route =
    pathPrefix("tags") {
      get {
        complete(tagService.findTags())
      } ~
      (path("streaming") & get) {
        complete(tagService.findTagsStreaming())
      } ~
      post {
        entity(as[Tag]) { tag =>
          logger.debug("tags POST")
          complete(
            tagService.upsertTag(tag)
              .map(postAndDeleteResponseTransformer)
          )
        }
      } ~
      (path(LongNumber) & delete) { tagId =>
        complete(
          tagService.deleteTag(tagId)
            .map(postAndDeleteResponseTransformer)
        )
      }
    }

  def tracksRoute: Route =
    pathPrefix("tracks") {
      (path(IdsSegment / "tags") & get) { trackIdSet =>
        cache(lfuCache, cacheKeyerFunction)(
          complete(
            tagService.findTagsByTrackIdSet(trackIdSet)
          )
        )
      } ~
      (path(IdsSegment / "tags" / "streaming") & get) { trackIdSet =>
        complete(
          tagService.findTagsByTrackIdSetStreaming(trackIdSet)
        )
      } ~
      path(LongNumber / "tags") { trackId =>
        (post &  entity(as[Set[Long]])) { idSet =>
          complete(
            tagService.upsertTracksTags(trackId, idSet)
              .map(postAndDeleteResponseTransformer)
          )
        } ~
          delete {
            complete(
              tagService.deleteTracksTags(trackId)
                .map(postAndDeleteResponseTransformer)
            )
          }
      }
    }

  def routes: Route =
    pagesRoute ~
    assetsRoute ~
    pathPrefix("api") {
      pathPrefix("v1") {
        tagsRoute ~
        tracksRoute
      }
    }
}
