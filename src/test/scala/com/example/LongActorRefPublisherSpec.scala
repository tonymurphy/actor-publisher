package com.example

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}


class LongActorRefPublisherSpec extends TestKit(ActorSystem("test-system")) with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  val decider: Supervision.Decider = {
    case e  => {
      println(s"Stopping Stream.. ${e.getMessage}")
      Supervision.Stop
    }
  }

  implicit val materializer = ActorMaterializer.create(ActorMaterializerSettings.create(system)
    .withDebugLogging(true)
    .withSupervisionStrategy(decider)
    .withAutoFusing(true), system)

  "Advert ID Actor" should "work" in {

  }

}
