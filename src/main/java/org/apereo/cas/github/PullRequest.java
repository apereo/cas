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

import org.apereo.cas.CasLabels;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.function.Predicate;

@Getter
@ToString(of = {"title", "url"}, includeFieldNames = false)
public class PullRequest {

    private final String number;

    private final String url;

    private final String commentsUrl;

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
                       @JsonProperty("user") final User user,
                       @JsonProperty("labels") final List<Label> labels,
                       @JsonProperty("milestone") final Milestone milestone,
                       @JsonProperty("state") final String state,
                       @JsonProperty("title") final String title,
                       @JsonProperty("number") final String number,
                       @JsonProperty("base") final Base base,
                       @JsonProperty("head") final Base head) {
        this.url = url;
        this.commentsUrl = commentsUrl;
        this.user = user;
        this.labels = labels;
        this.milestone = milestone;
        this.title = title;
        this.base = base;
        this.state = state;
        this.head = head;
        this.number = number;
    }

    public boolean isOpen() {
        return "open".equalsIgnoreCase(this.state);
    }

    public String getLabelsUrl() {
        return this.url.replace("pulls", "issues") + "/labels";
    }

    public String getMilestonesUrl() {
        return this.url.replace("pulls", "issues");
    }

    public boolean isTargetedAtMasterBranch() {
        return this.base.getRef().equalsIgnoreCase("master");
    }

    private static Predicate<Label> getLabelPredicateByName(final CasLabels name) {
        return l -> l.getName().contains(name.getTitle());
    }

    public boolean isLabeledAs(final CasLabels labelName) {
        return this.labels.stream().anyMatch(getLabelPredicateByName(labelName));
    }
}
