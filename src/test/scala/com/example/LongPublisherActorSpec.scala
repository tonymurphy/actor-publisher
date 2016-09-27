package com.example

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.example.LongPublisherMessage.Init
import org.reactivestreams.{Subscriber, Subscription}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.collection.immutable.Seq
import scala.concurrent.Await

class LongPublisherActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with FlatSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  val decider: Supervision.Decider = {
    case e  => {
      println(s"Stopping Stream.. ${e.getMessage}")
      Supervision.Stop
    }
  }

  "Advert ID Actor" should "work" in {
    implicit val materializer = ActorMaterializer.create(ActorMaterializerSettings.create(system)
      .withDebugLogging(true)
      .withSupervisionStrategy(decider)
      .withAutoFusing(true), system)

    val advertIdSource = Source.actorPublisher[Long](Props[LongPublisherActor])

    println(s"${advertIdSource}")

    val flow: Flow[Long,Long,NotUsed] = Flow[Long].map(x => x * 2)

    import scala.concurrent.duration._

    val subscriber: LongSubscriber = new LongSubscriber()
    val simple: ActorRef = flow.to(Sink.fromSubscriber(subscriber)).runWith(advertIdSource)
    import akka.pattern.ask

    implicit val timeout = Timeout(15 seconds)

//    subscriber.askForData()

    val longs: Vector[Long] = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9)
    simple ! Init(longs)

    Await.result(simple ? Request(10), 15 seconds)

  }

}

object LongPublisherMessage {
  case class Init(elements: Vector[Long])
}

// taken from akka 2.4.9 documentation, also http://liferepo.blogspot.co.uk/2015/01/creating-reactive-streams-components-on.html
class LongPublisherActor extends ActorPublisher[Long] {

  import LongPublisherMessage._
  import akka.stream.actor.ActorPublisherMessage._

  var buf = Vector.empty[Long]

  override def receive: Receive = {

    case Init(elements: Vector[Long]) => {
      buf ++= elements
    }
    case r: Request => {
      deliver()
      sender() ! true
    }
    case c: Cancel => {
      println("Cancelling Actor")
      context.stop(self)
    }

  }

  final def deliver(): Unit = {
    if(totalDemand > 0) {
      val (use, keep) = buf.splitAt(totalDemand.toInt)
      buf = keep
      if(use.length == 0) {
        println("at end of buffer")
        onComplete()
      }
      use foreach onNext
    } else {
      println("total demand = 0")
    }
  }

}

class LongSubscriber extends Subscriber[Long] {

  var adverts: Seq[Long] = Seq.empty
  var subscription: Option[Subscription] = None

  override def onError(t: Throwable): Unit = {
    println(s"error $t")
    subscription.map(s => s.cancel)
    adverts = Seq.empty
  }

  override def onSubscribe(s: Subscription): Unit = {
    if(subscription.isDefined) {
      println("already defined")
      subscription.map(s => s.cancel)
      throw new RuntimeException("Already subscribed")
    } else {
      println(s"onSubscribe ${s}")
      subscription = Some(s)
      askForData()
    }
  }

  override def onComplete(): Unit = {
    println("info onComplete")
  }

  // take in elements and store them
  override def onNext(e: Long): Unit = {
    println(s"adding $e")
    adverts :+ e
    subscription.get.request(1)

  }

  def askForData(): Unit = {
    subscription.get.request(1)
  }

}