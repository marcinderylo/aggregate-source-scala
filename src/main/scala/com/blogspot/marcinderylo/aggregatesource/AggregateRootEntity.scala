package com.blogspot.marcinderylo.aggregatesource

import scala.collection.mutable

/**
 * Base class for aggregate root entities that need some basic infrastructure for tracking state changes.
 */
abstract class AggregateRootEntity {
  /**
   * Type declaration for aggregate-internal event handlers.
   */
  protected type Handler = Function[AnyRef, Unit]

  private val stateChanges = mutable.ListBuffer[AnyRef]()
  private val handlers = mutable.HashMap[Class[_], Handler]()

  protected def registerHandler[T](handler:Handler)(implicit manifest:Manifest[T]) {
    if(handler == null) throw new NullPointerException("handler to register cannot be null")
    val eventType: Class[_] = manifest.runtimeClass
    handlers get(eventType) match {
      case None => handlers put (eventType, handler)
      case Some(_) => throw new IllegalArgumentException("there's already a handler registered for event class " + eventType)
    }
  }

  /**
   * Initializes this instance using the specified events.
   * @param events The events to initialize with.
   * @throws NullPointerException when events passed are null
   * @throws IllegalStateException when instance has changes
   */
  def initialize(events: Seq[AnyRef]) {
    if(events == null) throw new NullPointerException("events to initialize from cannot be null")
    if(hasChanges) throw new IllegalStateException("initialize cannot be called on instance with changes")
    events foreach play
  }

  /**
   * Applies the specified event to this instance and invokes the associated state handler.
   * @param event the event to apply.
   */
  protected final def apply(event:AnyRef) {
    if(event == null) throw new NullPointerException("event to apply cannot be null")
    play(event)
    record(event)
  }

  private def play(event:AnyRef) {
    val handler = handlers get(event.getClass)
    handler map (h => h(event))
  }

  private def record(event:AnyRef) {
    stateChanges append event
  }

  /**
   * Determines whether this instance has state changes.
   * @return true if this instance has state changes, false otherwise
   */
  def hasChanges:Boolean = ! stateChanges.isEmpty

  /**
   * @return the state changes applied to this instance.
   */
  def changes:Seq[AnyRef] = stateChanges.toSeq

  /**
   * Clears the state changes.
   */
  def clearChanges { stateChanges clear }
}
