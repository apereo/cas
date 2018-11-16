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

/**
 * A label that can be applied to a GitHub issue.
 *
 * @author Andy Wilkinson
 */
@Getter
public class Base {

    private final String label;
    private final String master;
    private final String sha;

    @JsonCreator
    public Base(@JsonProperty("label") final String label,
                @JsonProperty("master") final String master,
                @JsonProperty("sha") final String sha) {
        this.label = label;
        this.master = master;
        this.sha = sha;
    }

}
