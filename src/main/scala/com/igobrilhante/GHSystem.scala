package com.igobrilhante

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object GHSystem {

  def apply(): Behavior[GHSystem.Command] = Behaviors.empty

  trait Command
}
