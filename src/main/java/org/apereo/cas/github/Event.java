/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apereo.cas.github;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * An event that has been performed on an {@link Issue}.
 *
 * @author Andy Wilkinson
 */
@Getter
public class Event {

	private final Type type;

	private final OffsetDateTime creationTime;

	private final Label label;

	/**
	 * Creates a new {@code Event}.
	 *
	 * @param type the type of the event
	 * @param creationTime the timestamp of when the event was created
	 * @param label the label associated with the event
	 */
	@JsonCreator
	public Event(@JsonProperty("event") String type,
			@JsonProperty("created_at") OffsetDateTime creationTime,
			@JsonProperty("label") Label label) {
		this.type = Type.valueFrom(type);
		this.creationTime = creationTime;
		this.label = label;
	}

	/**
	 * The type of an {@link Event}.
	 *
	 * @author Andy Wilkinson
	 */
	public enum Type {

		/**
		 * The issue was closed by the actor.
		 */
		CLOSED("closed"),

		/**
		 * The issue was reopened by the actor.
		 */
		REOPENED("reopened"),

		/**
		 * The actor subscribed to receive notifications for an issue.
		 */
		SUBSCRIBED("subscribed"),

		/**
		 * The actor unsubscribed from receibing notifications for an issue.
		*/
		UNSUBSCRIBED("unsubscribed"),

		/**
		 * The issue was merged by the actor.
		 */
		MERGED("merged"),

		/**
		 * The issue was referenced from a commit message.
		 */
		REFERENCED("referenced"),

		/**
		 * The actor was {@code @mentioned} in an issue body.
		 */
		MENTIONED("mentioned"),

		/**
		 * The issue was assigned to the actor.
		 */
		ASSIGNED("assigned"),

		/**
		 * The actor was unassigned from the issue.
		 */
		UNASSIGNED("unassigned"),

		/**
		 * A label was added to the issue.
		 */
		LABELED("labeled"),

		/**
		 * A label was removed from the issue.
		 */
		UNLABELED("unlabeled"),

		/**
		 * The issue was added to a milestone.
		 */
		MILESTONED("milestoned"),

		/**
		 * The issue was removed from a milestone.
		 */
		DEMILESTONED("demilestoned"),

		/**
		 * The issue title was changed.
		 */
		RENAMED("renamed"),

		/**
		 * The issue was locked by the actor.
		 */
		LOCKED("locked"),

		/**
		 * The issue was unlocked by the actor.
		 */
		UNLOCKED("unlocked"),

		/**
		 * The pull request's branch was deleted.
		 */
		HEAD_REF_DELETED("head_ref_deleted"),

		/**
		 * The pull request's branch was restored.
		 */
		HEAD_REF_RESTORED("head_ref_restored");

		private String type;

		Type(String type) {
			this.type = type;
		}

		static Type valueFrom(String type) {
			for (Type value : values()) {
				if (type.equals(value.type)) {
					return value;
				}
			}
			throw new IllegalArgumentException(
					"'" + type + "' is not a valid event type");
		}
	}

}
