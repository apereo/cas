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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * A GitHub issue.
 *
 * @author Andy Wilkinson
 */
public class Issue {

	@Getter(AccessLevel.PACKAGE)
	private final String url;

	@Getter(AccessLevel.PACKAGE)
	private final String commentsUrl;

	@Getter(AccessLevel.PACKAGE)
	private final String eventsUrl;

	@Getter(AccessLevel.PACKAGE)
	private final String labelsUrl;

	@Getter
	private final User user;

	@Getter
	private List<Label> labels;

	@Getter
	private final Milestone milestone;

	@Getter
	private final PullRequest pullRequest;

	/**
	 * Creates a new {@code Issue}.
	 *
	 * @param url the url of the issue in the GitHub API
	 * @param commentsUrl the url of the comments on the issue in the GitHub API
	 * @param eventsUrl the url of the events on the issue in the GitHub API
	 * @param labelsUrl the url of the labels on the issue in the GitHub API
	 * @param user the user that created the issue
	 * @param labels the labels applied to the issue
	 * @param milestone the milestone applied to the issue
	 * @param pullRequest details of the pull request (if this issue is a pull request)
	 */
	@JsonCreator
	public Issue(@JsonProperty("url") String url,
			@JsonProperty("comments_url") String commentsUrl,
			@JsonProperty("events_url") String eventsUrl,
			@JsonProperty("labels_url") String labelsUrl, @JsonProperty("user") User user,
			@JsonProperty("labels") List<Label> labels,
			@JsonProperty("milestone") Milestone milestone,
			@JsonProperty("pull_request") PullRequest pullRequest) {
		this.url = url;
		this.commentsUrl = commentsUrl;
		this.eventsUrl = eventsUrl;
		this.labelsUrl = labelsUrl;
		this.user = user;
		this.labels = labels;
		this.milestone = milestone;
		this.pullRequest = pullRequest;
	}

	@Override
	public String toString() {
		return this.url;
	}

}
