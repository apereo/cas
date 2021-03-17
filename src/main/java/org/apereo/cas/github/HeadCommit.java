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
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@ToString(of = {"author", "id"})
public class HeadCommit {
    private final String id;

    private final String message;

    private final User author;

    private final User committer;

    private final OffsetDateTime timestamp;

    @JsonCreator
    public HeadCommit(@JsonProperty("id") final String id,
                      @JsonProperty("message") final String message,
                      @JsonProperty("timestamp") final OffsetDateTime timestamp,
                      @JsonProperty("author") final User author,
                      @JsonProperty("committer") final User committer) {
        this.id = id;
        this.message = message;
        this.author = author;
        this.committer = committer;
        this.timestamp = timestamp;
    }

}
