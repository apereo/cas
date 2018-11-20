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

public interface GitHubOperations {

    Page<Issue> getIssues(String organization, String repository);

    Page<PullRequest> getPullRequests(String organization, String repository);

    Page<Comment> getComments(Issue issue);

    Issue addLabel(Issue issue, String label);

    PullRequest addLabel(PullRequest pr, String label);

    Issue removeLabel(Issue issue, String label);

    Comment addComment(Issue issue, String comment);

    Comment addComment(PullRequest pullRequest, String comment);

    Issue close(Issue issue);

    Page<Event> getEvents(Issue issue);

    Page<Milestone> getMilestones(String organization, String name);

    Page<Label> getLabels(String organization, String name);

    void setMilestone(PullRequest pr, Milestone milestone);
}
