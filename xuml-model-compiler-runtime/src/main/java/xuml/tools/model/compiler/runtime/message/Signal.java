package xuml.tools.model.compiler.runtime.message;

import xuml.tools.model.compiler.runtime.Entity;
import xuml.tools.model.compiler.runtime.Event;

public class Signal<T> {

	private final Entity<T> entity;
	private final Event<T> event;

	public Signal(Entity<T> entity, Event<T> event) {
		this.entity = entity;
		this.event = event;
	}

	public Entity<T> getEntity() {
		return entity;
	}

	public Event<T> getEvent() {
		return event;
	}

}