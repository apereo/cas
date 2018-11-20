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

@Getter
@EqualsAndHashCode
public class Label {

    private final String name;
    private final String id;
    private final String url;
    private final String description;
    private final String color;

    @JsonCreator
    public Label(@JsonProperty("name") final String name,
                 @JsonProperty("id") final String id,
                 @JsonProperty("url") final String url,
                 @JsonProperty("description") final String description,
                 @JsonProperty("color") final String color) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.description = description;
        this.color = color;
    }

}
