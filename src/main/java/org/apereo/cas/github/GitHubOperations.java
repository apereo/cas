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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface GitHubOperations {

    Page<Issue> getIssues(String organization, String repository);

    Page<PullRequest> getPullRequests(String organization, String repository);

    PullRequest getPullRequest(String organization, String repository, String number);

    Page<Comment> getComments(Issue issue);

    Page<Comment> getComments(PullRequest issue);

    Issue addLabel(Issue issue, String label);

    PullRequest addLabel(PullRequest pr, String... label);
    
    PullRequest addLabel(PullRequest pr, List<String> labels);

    Issue removeLabel(Issue issue, String label);

    void removeLabel(PullRequest issue, String label);

    Comment addComment(Issue issue, String comment);

    Comment addComment(PullRequest pullRequest, String comment);

    Page<Comment> getComments(String organization, String name, String number);

    void removeComment(String organization, String name, String commentId);

    Issue close(Issue issue);

    Page<Event> getEvents(Issue issue);

    Page<Milestone> getMilestones(String organization, String name);

    Page<Label> getLabels(String organization, String name);

    void setMilestone(PullRequest pr, Milestone milestone);

    PullRequest mergeWithBase(String organization, String repository, PullRequest pr);

    boolean approve(String organization, String repository, PullRequest pr, boolean includeComment);

    boolean mergeIntoBase(String organization, String repository, PullRequest pr,
                          String commitTitle, String commitMessage,
                          String shaToMatch, String method);

    Page<PullRequestFile> getPullRequestFiles(String organization, String repository, String number);

    Workflows getWorkflowRuns(String organization, String repository, Branch branch,
                              Workflows.WorkflowRunEvent event, Workflows.WorkflowRunStatus status,
                              Commit commit, long page);

    default Workflows getWorkflowRuns(String organization, String repository, Branch branch,
                                      Workflows.WorkflowRunEvent event, Workflows.WorkflowRunStatus status) {
        return getWorkflowRuns(organization, repository, branch, event, status, null, 1);
    }

    default Workflows getWorkflowRuns(String organization, String repository, final Commit commit,
                                      final Workflows.WorkflowRunStatus status) {
        return getWorkflowRuns(organization, repository, null, null, status, commit, 1);
    }

    default Workflows getWorkflowRuns(String organization, String repository,
                                      Workflows.WorkflowRunEvent event, Workflows.WorkflowRunStatus status) {
        return getWorkflowRuns(organization, repository, null, event, status);
    }

    default Workflows getWorkflowRuns(String organization, String repository,
                                      Workflows.WorkflowRunEvent event) {
        return getWorkflowRuns(organization, repository, null, event, null);
    }

    default Workflows getWorkflowRuns(String organization, String repository,
                                      Workflows.WorkflowRunStatus status) {
        return getWorkflowRuns(organization, repository, null, null, status);
    }


    default Workflows getWorkflowRuns(String organization, String repository, Branch branch,
                                      Workflows.WorkflowRunStatus status) {
        return getWorkflowRuns(organization, repository, branch, null, status);
    }

    default Workflows getWorkflowRuns(String organization, String repository) {
        return getWorkflowRuns(organization, repository, null, null, null);
    }


    default Workflows getWorkflowRuns(String organization, String repository, long page) {
        return getWorkflowRuns(organization, repository, null, null, null, null, page);
    }

    Workflow getWorkflow(String organization, String repository, long workflowId);
    
    Page<CommitStatus> getPullRequestCommitStatus(String organization, String repository, String number);

    Page<CommitStatus> getPullRequestCommitStatus(PullRequest pr);

    CombinedCommitStatus getCombinedPullRequestCommitStatus(final String organization, final String repository, String ref);

    Page<Commit> getPullRequestCommits(String organization, String repository, String number);

    Commit getCommit(String organization, String repository, String branchOrSha);

    void closePullRequest(String organization, String repository, String number);
    
    void openPullRequest(String organization, String repository, String number);

    CheckRun getCheckRunsFor(String organization, String repository, String ref,
                             String checkName, String status, String filter);

    boolean createCheckRun(String organization, String repository, String name,
                           String ref, String status, String conclusion,
                           Map<String, String> output) throws Exception;

    boolean createStatus(String organization, String repository, String ref,
                         String state, String targetUrl, String description,
                         String context) throws Exception;

    boolean cancelWorkflowRun(String organization, String repository,
                              Workflows.WorkflowRun run) throws Exception;

    Page<Branch> getBranches(String organization, String name);

    void removeWorkflowRun(String organization, String name, Workflows.WorkflowRun run);

    void updatePullRequest(String organization, String name,
                           PullRequest pr, Map<String, ? extends Serializable> payload);

    Page<PullRequestReview> getPullRequestReviews(String organization, String name, PullRequest pr);
    
    Page<TimelineEntry> getPullRequestTimeline(String organization, String name, PullRequest pr);

    void rerunFailedWorkflowJobs(String organization, String name, Workflows.WorkflowRun run);

    PullRequestSearchResults searchPullRequests(String organization, String name, String sha);

    void assignPullRequest(String organization, String name, PullRequest pr, String... assignee);

    void unassignPullRequest(String organization, String name, PullRequest pr, String... assignee);

}
