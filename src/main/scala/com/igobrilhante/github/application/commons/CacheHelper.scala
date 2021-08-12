package com.igobrilhante.github.application.commons

import net.sf.ehcache.{Cache, CacheManager}

object CacheHelper {

  val GithubContributorsCache = "githubContributorsCache"
  private val cacheManager    = new CacheManager

  cacheManager.addCache(GithubContributorsCache)

  def getContributorsCache: Cache = cacheManager.getCache(GithubContributorsCache)

}
