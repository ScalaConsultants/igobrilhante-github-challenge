package com.igobrilhante.github.core.application.commons

case class CacheHeader private (etag: Option[String], lastModified: Option[Long])

object CacheHeader {

  def apply(etag: String, lastModified: Long): CacheHeader = CacheHeader(Some(etag), Some(lastModified))

  def withEtag(etag: String): CacheHeader = CacheHeader(Some(etag), None)

  def withLastModified(lastModified: Long): CacheHeader = CacheHeader(None, Some(lastModified))

}
