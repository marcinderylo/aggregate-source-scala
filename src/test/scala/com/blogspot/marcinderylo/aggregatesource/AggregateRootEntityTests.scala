package com.blogspot.marcinderylo.aggregatesource

import org.scalatest.{BeforeAndAfter, FeatureSpec}
import collection.mutable.ListBuffer

class AggregateRootEntityTests extends FeatureSpec with BeforeAndAfter {
  feature("with any instance") {

    scenario("applied event cannot be null") {
      val instance = new ApplyNullEventAggregateRootEntity
      intercept[NullPointerException] {
        instance applyNull
      }
    }

    scenario("initial events cannot be null") {
      intercept[NullPointerException] {
        new IntializeWithNullEventsAggregateRootEntity
      }
    }

    scenario("registered handler cannot be null") {
      intercept[NullPointerException] {
        new RegisterNullHandlerAggregateRootEntity
      }
    }

    scenario("can only register one event handler per event type") {
      intercept[IllegalArgumentException] {
        new RegisterSameEventHandlerTwiceAggregateRootEntity
      }
    }

    class IntializeWithNullEventsAggregateRootEntity extends AggregateRootEntity {
      initialize(null)
    }

    class RegisterNullHandlerAggregateRootEntity extends AggregateRootEntity {
      registerHandler[AnyRef](null)
    }

    class RegisterSameEventHandlerTwiceAggregateRootEntity extends AggregateRootEntity {
      val noOp: Handler = {
        case _ =>
      }
      registerHandler[AnyRef](noOp)
      registerHandler[AnyRef](noOp)
    }

    class ApplyNullEventAggregateRootEntity extends AggregateRootEntity {
      def applyNull {
        apply(null)
      }
    }
  }

  feature("with pristine instance") {

    scenario("clearChanges does nothing") {
      new WithPristineInstance {
        instance clearChanges
      }
    }

    scenario("hasChagnes returns false ") {
      new WithPristineInstance {
        assert(!instance.hasChanges)
      }
    }

    scenario("changes seq is empty") {
      new WithPristineInstance {
        assert(instance.changes === Seq())
      }
    }

    scenario("initialize does not throw") {
      new WithPristineInstance {
        instance initialize Seq(Int.box(1), "foo", new AnyRef)
      }
    }

    trait WithPristineInstance {
      val instance = new AggregateRootEntity {}
    }
  }

  feature("with initialized instance") {

    scenario("clearChanges does nothing") {
      new WithInitializedInstance {
        instance clearChanges
      }
    }

    scenario("hasChanges returns false") {
      new WithInitializedInstance {
        assert(!instance.hasChanges)
      }
    }

    scenario("changes seq is empty") {
      new WithInitializedInstance {
        assert(instance.changes === Seq())
      }
    }

    scenario("initialize does not throw") {
      new WithInitializedInstance {
        instance initialize Seq(Int.box(3), "baz")
      }
    }

    trait WithInitializedInstance {
      val instance = new AggregateRootEntity {
        initialize(Seq(Int.box(1), "bar"))
      }
    }
  }

  feature("with changed instance") {

    scenario("clearChanges does nothing") {
      new WithChangedInstance {
        instance clearChanges
      }
    }

    scenario("hasChanges returns true") {
      new WithChangedInstance {
        assert(instance.hasChanges)
      }
    }

    scenario("changes seq contains applied changes") {
      new WithChangedInstance {
        assert(instance.changes === appliedChanges)
      }
    }

    scenario("initialize does not throw") {
      new WithChangedInstance {
        intercept[IllegalStateException] {
          instance initialize Seq(Int.box(3), "baz")
        }
      }
    }

    trait WithChangedInstance {
      val appliedChanges = Seq(new AnyRef, "foobar", Int.box(17))
      val instance = new AggregateRootEntity {
        appliedChanges foreach (apply _)
      }
    }
  }

  feature("with changed then cleared instance") {

    scenario("clearChanges does nothing") {
      new WithChangedThenClearedInstance {
        instance clearChanges
      }
    }

    scenario("hasChanges returns false") {
      new WithChangedThenClearedInstance {
        assert(!instance.hasChanges)
      }
    }

    scenario("changes seq is empty") {
      new WithChangedThenClearedInstance {
        assert(instance.changes === Seq())
      }
    }

    scenario("initialize does not throw") {
      new WithChangedThenClearedInstance {
        instance initialize Seq(Int.box(3), "baz")
      }
    }

    trait WithChangedThenClearedInstance {
      val instance = new AggregateRootEntity {
        apply(Seq(Int.box(1)))
        apply("bar")
        clearChanges
      }
    }
  }

  feature("with initialized then changed then cleared instance") {

    scenario("clearChanges does nothing") {
      new WithInitializedThenChangedThenClearedInstance {
        instance clearChanges
      }
    }

    scenario("hasChanges returns false") {
      new WithInitializedThenChangedThenClearedInstance {
        assert(!instance.hasChanges)
      }
    }

    scenario("changes seq is empty") {
      new WithInitializedThenChangedThenClearedInstance {
        assert(instance.changes === Seq())
      }
    }

    scenario("initialize does not throw") {
      new WithInitializedThenChangedThenClearedInstance {
        instance initialize Seq(Int.box(3), "baz")
      }
    }

    trait WithInitializedThenChangedThenClearedInstance {
      val instance = new AggregateRootEntity {
        initialize(Seq(new AnyRef, Double.box(2.0)))
        apply("bar")
        clearChanges
      }
    }
  }

  feature("with instance with handlers") {

    scenario("intialize calls handler for each event") {
      new WithInstanceWithHandlers {
        val expectedEvents = Seq(new AnyRef, new AnyRef)
        instance initialize expectedEvents

        assert(instance.handlerCallCount === 2)
        assert(instance.playedEvents === expectedEvents)
      }
    }

    scenario("apply event calls event handler") {
      new WithInstanceWithHandlers {
        val event = new AnyRef
        instance doApply event
        assert(instance.handlerCallCount == 1)
        assert(instance.playedEvents == Seq(event))
      }
    }

    trait WithInstanceWithHandlers {
      val instance = new AggregateRootEntity {
        val playedEvents = ListBuffer[AnyRef]()
        var handlerCallCount = 0
        registerHandler[AnyRef] {event =>
          handlerCallCount += 1
          playedEvents append event
        }
        def doApply(event:AnyRef) {
          apply(event)
        }
      }
    }
  }

  feature("with instance without handlers") {
    scenario("initialize does not throw") {
      new WithInstanceWithoutHandlers {
        instance initialize Seq(new AnyRef)
      }
    }
    scenario("apply event does not throw") {
      new WithInstanceWithoutHandlers {
        instance doApply new AnyRef
      }
    }

    trait WithInstanceWithoutHandlers {
      val instance = new AggregateRootEntity {
        def doApply(event:AnyRef) {
          apply(event)
        }
      }
    }
  }
}
