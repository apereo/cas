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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@Getter
@ToString(of = {"title"})
@EqualsAndHashCode(of = "number")
public class Milestone implements Comparable<Milestone> {

    private final String title;
    private final String description;
    private final String url;
    private final String number;

    private final ZonedDateTime createdDate;
    private final ZonedDateTime updatedDate;
    private final ZonedDateTime dueDate;

    @JsonCreator
    public Milestone(@JsonProperty("title") final String title,
                     @JsonProperty("description") final String description,
                     @JsonProperty("url") final String url,
                     @JsonProperty("number") final String number,
                     @JsonProperty("created_at") final ZonedDateTime created,
                     @JsonProperty("updated_at") final ZonedDateTime updated,
                     @JsonProperty("due_on") final ZonedDateTime dueOn) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.number = number;
        
        this.createdDate = created;
        this.updatedDate = updated;
        this.dueDate = dueOn;
    }

    @Override
    public int compareTo(final Milestone o) {
        return this.dueDate.compareTo(o.getDueDate());
    }
}
