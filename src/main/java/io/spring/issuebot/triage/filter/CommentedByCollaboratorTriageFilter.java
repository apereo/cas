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

package io.spring.issuebot.triage.filter;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.spring.issuebot.triage.github.Comment;
import io.spring.issuebot.triage.github.GitHubOperations;
import io.spring.issuebot.triage.github.Issue;
import io.spring.issuebot.triage.github.Page;

/**
 * A {@code TriageFilter} that considers an issue has having been triaged if a
 * collaborator has commented on it.
 *
 * @author Andy Wilkinson
 */
final class CommentedByCollaboratorTriageFilter implements TriageFilter {

	private static final Logger log = LoggerFactory
			.getLogger(CommentedByCollaboratorTriageFilter.class);

	private final List<String> collaborators;

	private final GitHubOperations gitHub;

	CommentedByCollaboratorTriageFilter(List<String> collaborators,
			GitHubOperations gitHub) {
		this.collaborators = collaborators == null ? Collections.emptyList()
				: collaborators;
		this.gitHub = gitHub;
	}

	@Override
	public boolean triaged(Issue issue) {
		Page<Comment> page = this.gitHub.getComments(issue);
		while (page != null) {
			for (Comment comment : page.getContent()) {
				if (this.collaborators.contains(comment.getUser().getLogin())) {
					log.debug("{} has been triaged. It was commented on by {}",
							issue.getUrl(), comment.getUser().getLogin());
					return true;
				}
			}
			page = page.next();
		}

		return false;
	}

}
