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
import org.apereo.cas.Users;

import java.util.List;

@Getter
@ToString(of = {"title", "url", "user"}, includeFieldNames = false)
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
    private final long comments;

    private final String statusesUrl;
    private final String mergeableState;
    private final String mergeCommitSha;

    private final boolean mergeable;
    private final boolean rebaseable;
    private final boolean locked;
    private final boolean draft;

    private final Assignee assignee;

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
                       @JsonProperty("comments") final long comments,
                       @JsonProperty("commits") final long commits,
                       @JsonProperty("statuses_url") final String statusesUrl,
                       @JsonProperty("mergeable") final String mergeable,
                       @JsonProperty("merge_commit_sha") final String mergeCommitSha,
                       @JsonProperty("mergeable_state") final String mergeableState,
                       @JsonProperty("rebaseable") final String rebaseable,
                       @JsonProperty("assignee") final Assignee assignee,
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
        this.comments = comments;

        this.statusesUrl = statusesUrl;
        this.draft = draft == null ? Boolean.FALSE : draft;
        this.mergeable = mergeable != null && Boolean.parseBoolean(mergeable);
        this.rebaseable = rebaseable != null && Boolean.parseBoolean(rebaseable);
        this.locked = locked == null ? Boolean.FALSE : locked;
        this.mergeableState = mergeableState;
        this.mergeCommitSha = mergeCommitSha;

        this.assignee = assignee;
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

    public boolean isTargetedAtHerokuBranch() {
        return this.base.getRef().toLowerCase().startsWith("heroku");
    }

    public boolean isWorkInProgress() {
        return isLabeledAs(CasLabels.LABEL_WIP) || this.title.startsWith("WIP ");
    }

    public boolean isChore() {
        return this.title.startsWith("chore:") || this.title.startsWith("typo:");
    }

    public boolean isUnderReview() {
        return isLabeledAs(CasLabels.LABEL_UNDER_REVIEW) || this.title.startsWith("WIP ");
    }

    public boolean isLabeledAs(final CasLabels labelName) {
        return this.labels.stream().anyMatch(l -> l.getName().equalsIgnoreCase(labelName.getTitle()));
    }

    public boolean isRenovateBot() {
        return user.getLogin().equalsIgnoreCase("renovate[bot]");
    }

    public boolean isDependaBot() {
        return user.getLogin().equalsIgnoreCase("dependabot[bot]");
    }

    public boolean isApereoCasBot() {
        return user.getLogin().equalsIgnoreCase(Users.APEREO_CAS_BOT);
    }
    
    public boolean isBot() {
        return isApereoCasBot() || isDependaBot() || isRenovateBot();
    }
    
    public boolean isClosed() {
        return !isOpen();
    }

    public boolean isTargetedAtRepository(final String fullName) {
        return getBase().getRepository().getFullName().equalsIgnoreCase(fullName);
    }

    public boolean isBlocked() {
        return "blocked".equalsIgnoreCase(this.mergeableState);
    }

    
    public boolean isClean() {
        return "clean".equalsIgnoreCase(this.mergeableState);
    }
    
    public boolean isBehind() {
        return "behind".equalsIgnoreCase(this.mergeableState);
    }
}
