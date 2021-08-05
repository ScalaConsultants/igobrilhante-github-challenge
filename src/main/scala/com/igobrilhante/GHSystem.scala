package com.igobrilhante

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object GHSystem {

  trait Command

  def apply(): Behavior[GHSystem.Command] = Behaviors.empty

}
