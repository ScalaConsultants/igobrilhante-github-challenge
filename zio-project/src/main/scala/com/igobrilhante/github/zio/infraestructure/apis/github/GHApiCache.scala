package com.igobrilhante.github.zio.infraestructure.apis.github

import scala.concurrent.ExecutionContext

import com.igobrilhante.github.core.entities.GHContributor
import net.sf.ehcache.CacheManager
import scalacache.ehcache.EhcacheCache
import scalacache.{Cache, Flags}

class GHApiCache()(implicit val ec: ExecutionContext) {

  type CacheType = List[GHContributor]

  val cacheManager          = new CacheManager
  val underlying            = cacheManager.getCache("githubContributorsCache")
  implicit val flags: Flags = Flags(readsEnabled = true)

  implicit val cache: Cache[CacheType] = EhcacheCache(underlying)

}
