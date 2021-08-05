package com.igobrilhante.github.commons

import net.sf.ehcache.{Cache, CacheManager}

object CacheHelper {

  private val cacheManager = new CacheManager

  val GithubContributorsCache = "githubContributorsCache"

  cacheManager.addCache(GithubContributorsCache)

  def getContributorsCache: Cache = cacheManager.getCache(GithubContributorsCache)

}
