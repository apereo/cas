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

package org.apereo.cas.feedback;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apereo.cas.IssueListener;
import org.apereo.cas.github.Comment;
import org.apereo.cas.github.Event;
import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.Issue;
import org.apereo.cas.github.Label;
import org.apereo.cas.github.Page;

/**
 * An {@link IssueListener} that processes issues that are waiting for feedback.
 *
 * @author Andy Wilkinson
 */
final class FeedbackIssueListener implements IssueListener {

	private final GitHubOperations gitHub;

	private final String labelName;

	private final List<String> collaborators;

	private final FeedbackListener feedbackListener;

	FeedbackIssueListener(GitHubOperations gitHub, String labelName,
			List<String> collaborators, String username,
			FeedbackListener feedbackListener) {
		this.gitHub = gitHub;
		this.labelName = labelName;
		this.collaborators = new ArrayList<>(collaborators);
		this.collaborators.add(username);
		this.feedbackListener = feedbackListener;
	}

	@Override
	public void onOpenIssue(Issue issue) {
		if (waitingForFeedback(issue)) {
			OffsetDateTime waitingSince = getWaitingSince(issue);
			if (waitingSince != null) {
				processWaitingIssue(issue, waitingSince);
			}
		}
	}

	private void processWaitingIssue(Issue issue, OffsetDateTime waitingSince) {
		if (commentedSince(waitingSince, issue)) {
			this.feedbackListener.feedbackProvided(issue);
		}
		else {
			this.feedbackListener.feedbackRequired(issue, waitingSince);
		}
	}

	private boolean waitingForFeedback(Issue issue) {
		return issue.getPullRequest() == null && labelledAsWaitingForFeedback(issue);
	}

	private boolean labelledAsWaitingForFeedback(Issue issue) {
		for (Label label : issue.getLabels()) {
			if (this.labelName.equals(label.getName())) {
				return true;
			}
		}
		return false;
	}

	private OffsetDateTime getWaitingSince(Issue issue) {
		OffsetDateTime createdAt = null;
		Page<Event> page = this.gitHub.getEvents(issue);
		while (page != null) {
			for (Event event : page.getContent()) {
				if (Event.Type.LABELED.equals(event.getType())
						&& this.labelName.equals(event.getLabel().getName())) {
					createdAt = event.getCreationTime();
				}
			}
			page = page.next();
		}
		return createdAt;
	}

	private boolean commentedSince(OffsetDateTime waitingForFeedbackSince, Issue issue) {
		Page<Comment> page = this.gitHub.getComments(issue);
		while (page != null) {
			for (Comment comment : page.getContent()) {
				if (!this.collaborators.contains(comment.getUser().getLogin())
						&& comment.getCreationTime().isAfter(waitingForFeedbackSince)) {
					return true;
				}
			}
			page = page.next();
		}
		return false;
	}

}
