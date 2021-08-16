package com.igobrilhante.github.zio.interfaces.adapters

import com.igobrilhante.github.adapters.json.circle.CircleJsonEntitiesAdapters
import org.http4s.circe.CirceEntityEncoder

/** Json serializer adapter for http4s using circle.
  */
object Http4sJsonEntitiesAdapters extends CircleJsonEntitiesAdapters with CirceEntityEncoder
