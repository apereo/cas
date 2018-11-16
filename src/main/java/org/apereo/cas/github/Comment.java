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

import java.time.OffsetDateTime;

/**
 * A comment that has been made on a GitHub issue.
 *
 * @author Andy Wilkinson
 */
@Getter
public final class Comment {

    private final User user;

    private final OffsetDateTime creationTime;

    /**
     * Creates a new comment that was authored by the given {@code user} at the given
     * {@code creationTime}.
     *
     * @param user         the user
     * @param creationTime the creation time
     */
    @JsonCreator
    public Comment(@JsonProperty("user") final User user,
                   @JsonProperty("created_at") final OffsetDateTime creationTime) {
        this.user = user;
        this.creationTime = creationTime;
    }

}
