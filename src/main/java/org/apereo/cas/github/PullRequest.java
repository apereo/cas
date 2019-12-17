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

    private final String body;

    private final Base base;

    private final Base head;

    private final long changedFiles;
    private final long deletions;
    private final long additions;
    private final long commits;

    private final String statusesUrl;

    private final boolean mergeable;
    private final boolean locked;
    private final boolean draft;

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
                       @JsonProperty("head") final Base head,
                       @JsonProperty("body") final String body,
                       @JsonProperty("changed_files") final long changedFiles,
                       @JsonProperty("deletions") final long deletions,
                       @JsonProperty("additions") final long additions,
                       @JsonProperty("commits") final long commits,
                       @JsonProperty("statuses_url") final String statusesUrl,
                       @JsonProperty("mergeable") final String mergeable,
                       @JsonProperty("locked") final Boolean locked,
                       @JsonProperty("draft") final Boolean draft) {
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
        this.body = body;

        this.changedFiles = changedFiles;
        this.deletions = deletions;
        this.additions = additions;
        this.commits = commits;

        this.statusesUrl = statusesUrl;
        this.draft = draft;
        this.mergeable = mergeable != null && Boolean.parseBoolean(mergeable);
        this.locked = locked;
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

    public boolean isLabeledAs(final CasLabels labelName) {
        return this.labels.stream().anyMatch(l -> l.getName().equalsIgnoreCase(labelName.getTitle()));
    }
}
