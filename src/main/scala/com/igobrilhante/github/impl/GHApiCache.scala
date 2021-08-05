package com.igobrilhante.github.impl
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

import com.igobrilhante.github.models.GHContributor
import net.sf.ehcache.{Cache => UnderlyingCache, _}
import scalacache.{Cache, _}
import scalacache.ehcache.EhcacheCache
import scalacache.memoization._
import scalacache.modes.scalaFuture._

class GHApiCache()(implicit val ec: ExecutionContext) {

  type CacheType = List[GHContributor]

  val cacheManager                = new CacheManager
  val underlying: UnderlyingCache = cacheManager.getCache("githubContributorsCache")
  implicit val flags              = Flags(readsEnabled = true)

  implicit val cache: Cache[CacheType] = EhcacheCache(underlying)


}
