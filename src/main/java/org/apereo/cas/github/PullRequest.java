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

import java.util.List;

/**
 * Details of a GitHub pull request.
 *
 * @author Andy Wilkinson
 */
@Getter
public class PullRequest {

    private final String url;

    private final String commentsUrl;

    private final String labelsUrl;

    private final User user;

    private final Milestone milestone;

    private final List<Label> labels;

    private final String state;

    private final String title;

    private final Base base;
    private final Base head;
    
    @JsonCreator
    public PullRequest(@JsonProperty("url") final String url,
                       @JsonProperty("comments_url") final String commentsUrl,
                       @JsonProperty("labels_url") final String labelsUrl,
                       @JsonProperty("user") final User user,
                       @JsonProperty("labels") final List<Label> labels,
                       @JsonProperty("milestone") final Milestone milestone,
                       @JsonProperty("state") final String state,
                       @JsonProperty("title") final String title,
                       @JsonProperty("base") final Base base,
                       @JsonProperty("head") final Base head) {
        this.url = url;
        this.commentsUrl = commentsUrl;
        this.user = user;
        this.labelsUrl = labelsUrl;
        this.labels = labels;
        this.milestone = milestone;
        this.title = title;
        this.base = base;
        this.state = state;
        this.head = head;
    }

    public boolean isOpen() {
        return "open".equalsIgnoreCase(this.state);
    }
}
