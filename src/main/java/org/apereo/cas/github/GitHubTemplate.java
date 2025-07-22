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

import org.apereo.cas.Memes;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Central class for interacting with GitHub's REST API.
 *
 * @author Andy Wilkinson
 */
@RequiredArgsConstructor
@Slf4j
public class GitHubTemplate implements GitHubOperations {

    private static final String API_GITHUB_REPOS = "https://api.github.com/repos/";

    private final RestOperations rest;

    private final LinkParser linkParser;

    public GitHubTemplate(final String token, final LinkParser linkParser) {
        this(createDefaultRestTemplate(token), linkParser);
    }

    static RestTemplate createDefaultRestTemplate(final String token) {
        var rest = new RestTemplate();
        rest.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(final ClientHttpResponse response) throws IOException {
                if (response.getStatusCode() == HttpStatus.FORBIDDEN && response
                    .getHeaders().getFirst("X-RateLimit-Remaining").equals("0")) {
                    var dt = new Date(Long.parseLong(response.getHeaders().getFirst("X-RateLimit-Reset")) * 1000);
                    log.warn("Rate limit exceeded. Limit will reset at {}", dt);
                }
            }
        });
        var bufferingClient = new BufferingClientHttpRequestFactory(
            new HttpComponentsClientHttpRequestFactory());
        rest.setRequestFactory(bufferingClient);
        rest.setInterceptors(Collections
            .singletonList(new BasicAuthorizationInterceptor(token)));
        rest.setMessageConverters(
            Arrays.asList(new ErrorLoggingMappingJackson2HttpMessageConverter()));
        return rest;
    }

    @Override
    public Page<Issue> getIssues(final String organization, final String repository) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/issues";
        return getPage(url, Issue[].class);
    }

    @Override
    public Page<PullRequest> getPullRequests(final String organization, final String repository) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls?state=open";
        val headers = new LinkedMultiValueMap<>(Map.of("Accept", List.of("application/vnd.github+json")));
        return getPage(url, PullRequest[].class, Map.of(), headers);
    }

    @Override
    public PullRequest getPullRequest(final String organization, final String repository, final String number) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls/" + number;
        val headers = new LinkedMultiValueMap<>(Map.of("Accept", List.of("application/vnd.github+json")));
        return getSinglePage(url, PullRequest.class, Map.of(), headers);
    }

    @Override
    public void closePullRequest(final String organization, final String repository, final String number) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls/" + number;
        var uri = URI.create(url);
        log.info("Closing to pull request {}", uri);

        final Map<String, String> body = new HashMap<>();
        body.put("state", "closed");
        var response = this.rest.exchange(new RequestEntity<>(body, HttpMethod.PATCH, uri), PullRequest.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to close to pull request. Response status: {}", response.getStatusCode());
        }
    }

    @Override
    public void openPullRequest(final String organization, final String repository, final String number) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls/" + number;
        var uri = URI.create(url);
        final Map<String, String> body = new HashMap<>();
        body.put("state", "open");
        var response = this.rest.exchange(new RequestEntity<>(body, HttpMethod.PATCH, uri), PullRequest.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to open to pull request. Response status: {}", response.getStatusCode());
        }
    }

    @Override
    public Page<Comment> getComments(final Issue issue) {
        return getPage(issue.getCommentsUrl(), Comment[].class);
    }

    @Override
    public Page<Comment> getComments(final PullRequest pr) {
        return getPage(pr.getCommentsUrl(), Comment[].class);
    }

    @Override
    public Page<Event> getEvents(final Issue issue) {
        return getPage(issue.getEventsUrl(), Event[].class);
    }

    @Override
    public Page<PullRequestFile> getPullRequestFiles(final String organization, final String repository, final String number) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls/" + number + "/files";
        return getPage(url, PullRequestFile[].class);
    }

    @Override
    @SneakyThrows
    public void updatePullRequest(final String organization, final String repository,
                                  final PullRequest pr, final Map<String, ? extends Serializable> payload) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls/" + pr.getNumber();
        var uri = URI.create(url);
        log.info("Closing to pull request {}", uri);
        var response = this.rest.exchange(new RequestEntity(payload, HttpMethod.PATCH, uri), PullRequest.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to update to pull request. Response status: {}", response.getStatusCode());
        }
    }

    @Override
    public Page<PullRequestReview> getPullRequestReviews(final String organization, final String name, final PullRequest pr) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/pulls/" + pr.getNumber() + "/reviews";
        return getPage(url, PullRequestReview[].class);
    }

    @Override
    public Page<TimelineEntry> getPullRequestTimeline(final String organization, final String name, final PullRequest pr) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/issues/" + pr.getNumber() + "/timeline";
        return getPage(url, TimelineEntry[].class);
    }

    @Override
    @SneakyThrows
    public Workflows getWorkflowRuns(final String organization, final String repository, final Branch branch,
                                     final Workflows.WorkflowRunEvent event, final Workflows.WorkflowRunStatus status,
                                     final Commit commit, final long page) {
        var url = API_GITHUB_REPOS + organization + '/' + repository + "/actions/runs";
        var urlBuilder = new URIBuilder(url);

        if (branch != null) {
            urlBuilder.addParameter("branch", branch.getName());
        }
        if (event != null) {
            urlBuilder.addParameter("event", event.getName());
        }
        if (status != null) {
            urlBuilder.addParameter("status", status.getName());
        }
        if (commit != null) {
            urlBuilder.addParameter("head_sha", commit.getSha());
        }

        urlBuilder.addParameter("per_page", "50");
        urlBuilder.addParameter("page", String.valueOf(page));

        val headers = new LinkedMultiValueMap<>(Map.of("Accept", List.of("application/vnd.github.v3+json")));
        return getSinglePage(urlBuilder.toString(), Workflows.class, Map.of(), headers);
    }

    @Override
    public PullRequestSearchResults searchPullRequests(final String organization, final String repository, final String sha) {
        var url = "https://api.github.com/search/issues?q=" + sha + "+repo:" + organization + '/' + repository + "+type:pr";
        val headers = new LinkedMultiValueMap<>(Map.of("Accept", List.of("application/vnd.github.v3+json")));
        return getSinglePage(url, PullRequestSearchResults.class, Map.of(), headers);
    }

    @Override
    public void rerunFailedWorkflowJobs(final String organization, final String name, final Workflows.WorkflowRun run) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/actions/runs/" + run.getId() + "/rerun-failed-jobs";
        val responseEntity = this.rest.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Map.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Successfully rerun failed workflow run id {}", run);
        } else {
            log.warn("Failed to rerun failed workflow jobs. Response status: {}", responseEntity.getStatusCode());
        }
    }

    @Override
    public Page<CommitStatus> getPullRequestCommitStatus(final String organization, final String repository, final String number) {
        val pr = getPullRequest(organization, repository, number);
        return getPullRequestCommitStatus(pr);
    }

    @Override
    public Page<CommitStatus> getPullRequestCommitStatus(final PullRequest pr) {
        return getPage(pr.getStatusesUrl(), CommitStatus[].class);
    }

    @Override
    public CombinedCommitStatus getCombinedPullRequestCommitStatus(final String organization, final String repository, String ref) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/commits/" + ref + "/status";
        return getSinglePage(url, CombinedCommitStatus.class);
    }

    @Override
    public Page<Commit> getPullRequestCommits(final String organization, final String repository, final String number) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls/" + number + "/commits";
        return getPage(url, Commit[].class);
    }

    @Override
    public Commit getCommit(final String organization, final String repository, final String branchOrSha) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/commits/" + branchOrSha;
        return getSinglePage(url, Commit.class);
    }

    @Override
    public boolean mergeIntoBase(final String organization, final String repository, final PullRequest pr,
                                 final String commitTitle, final String commitMessage,
                                 final String shaToMatch, final String method) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls/" + pr.getNumber() + "/merge";

        val map = new LinkedHashMap<>();
        if (StringUtils.hasText(commitTitle)) {
            map.put("commit_title", commitTitle);
        }
        if (StringUtils.hasText(commitMessage)) {
            map.put("commit_message", commitMessage);
        }
        if (StringUtils.hasText(shaToMatch)) {
            map.put("sha", shaToMatch);
        }
        if (StringUtils.hasText(method)) {
            map.put("merge_method", method);
        } else {
            map.put("merge_method", "squash");
        }
        val responseEntity = this.rest.exchange(url, HttpMethod.PUT, new HttpEntity<>(map), Map.class);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean approve(final String organization, final String repository, final PullRequest pr, final boolean includeComment) {
        try {
            val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls/" + pr.getNumber() + "/reviews";
            val params = new HashMap<String, String>();
            params.put("commit_id", pr.getHead().getSha());
            if (includeComment) {
                var template = IOUtils.toString(new ClassPathResource("template-pr-approved.md").getInputStream(), StandardCharsets.UTF_8);
                template = template.replace("${link}", Memes.PULL_REQUEST_APPROVED.select());
                params.put("body", template);
            }
            params.put("event", "APPROVE");
            val responseEntity = rest.exchange(new RequestEntity<>(params, HttpMethod.POST, URI.create(url)), Map.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error approving PR", e);
        }
        return false;
    }

    @Override
    public PullRequest mergeWithBase(final String organization, final String repository, final PullRequest pr) {
        if (pr.getHead().getRepository().isFork()) {
            val url = API_GITHUB_REPOS + organization + '/' + repository + "/pulls/" + pr.getNumber() + "/update-branch";
//            val params = new HashMap<String, String>();
//            params.put("expected_head_sha", pr.getHead().getSha());
            val responseEntity = this.rest.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(new LinkedMultiValueMap<>(Map.of("Accept", List.of("application/vnd.github.lydian-preview+json")))), Map.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Merged pull request {} with base", pr);
            } else {
                log.error("Unable to merge pull request {} with base {}", pr, responseEntity);
            }
            return pr;
        }

        val url = API_GITHUB_REPOS + organization + '/' + repository + "/merges";
        var uri = URI.create(url);
        var body = new HashMap<String, String>();
        val prBranch = pr.getHead().getRef();
        body.put("base", prBranch);

        val targetBranch = pr.getBase().getRef();
        body.put("head", targetBranch);

        body.put("commit_message", "Merged branch " + targetBranch + " into " + prBranch);

        val response = this.rest.exchange(new RequestEntity<>(body, HttpMethod.POST, uri), Map.class);
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            log.debug("Pull request [{}] already contains the [{}]; nothing to merge", targetBranch, pr);
        } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
            log.warn("Pull request [{}] has a merge conflict and cannot be merged with [{}]", pr, targetBranch);
        } else if (response.getStatusCode() == HttpStatus.CREATED) {
            log.info("Pull request [{}] is successfully merged with head [{}]", pr, targetBranch);
        } else {
            log.warn("Unable to handle merge with base; message [{}], status [{}]",
                response.getBody(), response.getStatusCode());
        }
        return pr;
    }

    @Override
    public Page<Milestone> getMilestones(final String organization, final String name) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/milestones?state=open";
        return getPage(url, Milestone[].class);
    }

    @Override
    public Page<Label> getLabels(final String organization, final String name) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/labels";
        return getPage(url, Label[].class);
    }

    @Override
    public void setMilestone(final PullRequest pr, final Milestone milestone) {
        var uri = URI.create(pr.getMilestonesUrl());
        log.info("Adding milestone {} to pull request {}", milestone, uri);

        final Map<String, String> body = new HashMap<>();
        body.put("milestone", milestone.getNumber());

        var response = this.rest.exchange(new RequestEntity<>(body, HttpMethod.PATCH, uri), PullRequest.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to add milestone to pull request. Response status: {}", response.getStatusCode());
        }
    }

    @Override
    public PullRequest addLabel(final PullRequest pr, final List<String> labels) {
        if (!labels.isEmpty()) {
            val uri = URI.create(pr.getLabelsUrl());
            log.info("Adding labels {} to pull request {}", labels, pr);
            var response = this.rest.exchange(
                new RequestEntity<>(labels, HttpMethod.POST, uri),
                Label[].class);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.warn("Failed to add label to pull request. Response status: {}", response.getStatusCode());
            }
        }
        return pr;
    }

    @Override
    public PullRequest addLabel(final PullRequest pr, final String... label) {
        return addLabel(pr, Arrays.asList(label));
    }

    @Override
    public Issue addLabel(final Issue issue, final String labelName) {
        var uri = URI.create(issue.getLabelsUrl().replace("{/name}", ""));
        log.info("Adding label {} to {}", labelName, uri);
        var response = this.rest.exchange(
            new RequestEntity<>(Arrays.asList(labelName), HttpMethod.POST, uri),
            Label[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to add label to issue. Response status: {}", response.getStatusCode());
        }
        return new Issue(issue.getUrl(), issue.getCommentsUrl(), issue.getEventsUrl(),
            issue.getLabelsUrl(), issue.getUser(), Arrays.asList(response.getBody()),
            issue.getMilestone(), issue.getPullRequest());
    }

    @Override
    @SneakyThrows
    public void removeLabel(final PullRequest pullRequest, final String label) {
        var encodedName = new URI(null, null, label, null).toString();
        val url = pullRequest.getLabelsUrl() + '/' + encodedName;
        log.info("Removing label {} from pull request {} using {}", label, pullRequest, url);
        try {
            rest.exchange(new RequestEntity<Void>(HttpMethod.DELETE, URI.create(url)), Label[].class);
        } catch (final Exception e) {
            log.warn("Failed to remove label from pull request. Response status: {}", e.getMessage());
        }
        pullRequest.getLabels().removeIf(l -> l.getName().equalsIgnoreCase(label));
    }

    @Override
    public Issue removeLabel(final Issue issue, final String labelName) {
        final String encodedName;
        try {
            encodedName = new URI(null, null, labelName, null).toString();
        } catch (final URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        log.info("Removing label {} from issue {}", labelName, issue);
        var response = this.rest.exchange(
            new RequestEntity<Void>(HttpMethod.DELETE, URI.create(
                issue.getLabelsUrl().replace("{/name}", '/' + encodedName))),
            Label[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to remove label from issue. Response status: {}", response.getStatusCode());
        }
        return new Issue(issue.getUrl(), issue.getCommentsUrl(), issue.getEventsUrl(),
            issue.getLabelsUrl(), issue.getUser(), Arrays.asList(response.getBody()),
            issue.getMilestone(), issue.getPullRequest());
    }

    @Override
    public Comment addComment(final Issue issue, final String comment) {
        var body = new HashMap<>();
        body.put("body", comment);
        return rest.postForEntity(issue.getCommentsUrl(), body, Comment.class).getBody();
    }

    @Override
    public Comment addComment(final PullRequest pullRequest, final String comment) {
        var body = new HashMap<>();
        body.put("body", comment);
        return this.rest.postForEntity(pullRequest.getCommentsUrl(), body, Comment.class).getBody();
    }

    @Override
    public Page<Comment> getComments(final String organization, final String name, final String number) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/issues/" + number + "/comments";
        return getPage(url, Comment[].class);
    }

    @Override
    public void removeComment(final String organization, final String name, final String commentId) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/issues/comments/" + commentId;
        var response = this.rest.exchange(new RequestEntity<Void>(HttpMethod.DELETE, URI.create(url)), Comment.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Failed to remove comment. Response status: {}", response.getStatusCode());
        }
    }

    @Override
    public void removeWorkflowRun(final String organization, final String name, final Workflows.WorkflowRun run) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/actions/runs/" + run.getId();
        try {
            var response = this.rest.exchange(new RequestEntity<Void>(HttpMethod.DELETE, URI.create(url)), Workflows.WorkflowRun.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.debug("Failed to remove workflow run {} from {}. Response status: {}",
                    run.getId(), organization + '/' + name, response.getStatusCode());
            }
        } catch (final HttpClientErrorException.Forbidden e) {
            log.error("{}: Cannot delete workflow run using {}: {}", e.getStatusText(), url, e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Issue close(final Issue issue) {
        var body = new HashMap<>();
        body.put("state", "closed");
        var response = this.rest.exchange(
            new RequestEntity<>(body, HttpMethod.PATCH, URI.create(issue.getUrl())),
            Issue.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to close issue. Response status: {}", response.getStatusCode());
        }
        return response.getBody();
    }

    @Override
    public boolean createCheckRun(final String organization, final String repository, final String name,
                                  final String ref, final String status, final String conclusion,
                                  final Map<String, String> output) throws Exception {
        val tz = TimeZone.getTimeZone("UTC");
        val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        val currentTime = df.format(new Date());
        val body = new HashMap<String, Object>();
        body.put("name", name);
        body.put("head_sha", ref);
        body.put("status", status);
        body.put("started_at", currentTime);
        body.put("conclusion", conclusion);
        body.put("completed_at", currentTime);
        body.put("output", output);
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/check-runs";

        val headers = new LinkedMultiValueMap<>(Map.of("Accept", List.of("application/vnd.github+json")));
        val response = this.rest.exchange(new RequestEntity<>(body, headers, HttpMethod.POST, new URI(url)), Map.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public CheckRun getCheckRunsFor(final String organization, final String repository, final String ref,
                                    final String checkName, final String status, final String filter) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/commits/" + ref + "/check-runs";
        var builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(checkName)) {
            builder.queryParam("check_name", checkName);
        }
        if (StringUtils.hasText(status)) {
            builder.queryParam("status", status);
        }
        if (StringUtils.hasText(filter)) {
            builder.queryParam("filter", filter);
        }
        return getSinglePage(builder.build().toUriString(), CheckRun.class, Map.of(),
            new LinkedMultiValueMap<>(Map.of("Accept", List.of("application/vnd.github+json"))));
    }

    @Override
    public boolean createStatus(final String organization, final String repository, final String ref,
                                final String state, final String targetUrl, final String description,
                                final String context) throws Exception {
        val body = new HashMap<>();
        body.put("state", state);
        body.put("context", context);
        body.put("target_url", targetUrl);
        body.put("description", description);
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/statuses/" + ref;
        val response = this.rest.exchange(new RequestEntity<>(body, HttpMethod.POST, new URI(url)), Map.class);
        return response.getStatusCode().is2xxSuccessful();

    }

    @Override
    public Page<Branch> getBranches(final String organization, final String name) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/branches";
        return getPage(url, Branch[].class);
    }

    @Override
    public boolean cancelWorkflowRun(final String organization,
                                     final String repository,
                                     final Workflows.WorkflowRun run) throws Exception {
        val body = new HashMap<>();
        val url = run.getCancelUrl();
        val response = this.rest.exchange(new RequestEntity<>(body, HttpMethod.POST, new URI(url)), Map.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void assignPullRequest(final String organization, final String name, final PullRequest pr, final String... assignee) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/issues/" + pr.getNumber() + "/assignees";
        val body = new HashMap<String, Object>();
        body.put("assignees", List.of(assignee));
        var response = this.rest.exchange(new RequestEntity<>(body, HttpMethod.POST, URI.create(url)), Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Failed to assign pull request. Response status: {}", response.getStatusCode());
        }
    }

    @Override
    public void unassignPullRequest(final String organization, final String name, final PullRequest pr, final String... assignee) {
        val url = API_GITHUB_REPOS + organization + '/' + name + "/issues/" + pr.getNumber() + "/assignees";
        val body = new HashMap<String, Object>();
        body.put("assignees", List.of(assignee));
        var response = this.rest.exchange(new RequestEntity<>(body, HttpMethod.DELETE, URI.create(url)), Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Failed to unassign pull request. Response status: {}", response.getStatusCode());
        }
    }

    @Override
    public Workflow getWorkflow(String organization, String repository, long workflowId) {
        val url = API_GITHUB_REPOS + organization + '/' + repository + "/actions/workflows/" + workflowId;
        var builder = UriComponentsBuilder.fromUriString(url);
        return getSinglePage(builder.build().toUriString(), Workflow.class, Map.of(),
                new LinkedMultiValueMap<>(Map.of("Accept", List.of("application/vnd.github+json"))));
    }

    private static final class ErrorLoggingMappingJackson2HttpMessageConverter
        extends MappingJackson2HttpMessageConverter {

        private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");

        @Override
        public Object read(final Type type, final Class<?> contextClass,
                           final HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
            try {
                return super.read(type, contextClass, inputMessage);
            } catch (final IOException ex) {
                throw ex;
            } catch (final HttpMessageNotReadableException ex) {
                log.error("Failed to create {} from {}", type.getTypeName(),
                    read(inputMessage), ex);
                throw ex;
            }
        }

        private String read(final HttpInputMessage inputMessage) throws IOException {
            return StreamUtils.copyToString(inputMessage.getBody(), CHARSET_UTF_8);
        }

    }

    @RequiredArgsConstructor
    private static class BasicAuthorizationInterceptor
        implements ClientHttpRequestInterceptor {

        private final String token;

        @Override
        public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
                                            final ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().add("Authorization", "Bearer " + token);
            return execution.execute(request, body);
        }

    }

    private <T> Page<T> getPage(final String url, final Class<T[]> type, final Map params) {
        return getPage(url, type, params, new HttpHeaders());
    }

    private <T> Page<T> getPage(final String url, final Class<T[]> type, final Map params, final MultiValueMap headers) {
        try {
            if (!StringUtils.hasText(url)) {
                return null;
            }
            var hd = new HttpHeaders(headers);
            var contents = this.rest.exchange(url, HttpMethod.GET, new HttpEntity<>(hd), type, params);
            var body = Arrays.asList(type.cast(contents.getBody()));
            return new StandardPage<>(body, () -> getPage(getNextUrl(contents), type));
        } catch (final HttpMessageNotReadableException ex) {
            log.error(ex.getMessage(), ex);
        }
        return new StandardPage<>(List.of(), () -> null);
    }

    private <T> Page<T> getPage(final String url, final Class<T[]> type) {
        return getPage(url, type, Map.of());
    }

    private <T> T getSinglePage(final String url, final Class<T> type) {
        return getSinglePage(url, type, Map.of());
    }

    private <T> T getSinglePage(final String url, final Class<T> type, final Map params) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        final ResponseEntity<T> contents = this.rest.getForEntity(url, type, params);
        return contents.getStatusCode().is2xxSuccessful() ? contents.getBody() : null;
    }

    private <T> T getSinglePage(final String url, final Class<T> type, final Map params, final MultiValueMap headers) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        val hd = new HttpHeaders(headers);
        final ResponseEntity<T> contents = this.rest.exchange(url, HttpMethod.GET, new HttpEntity<>(hd), type, params);
        return contents.getStatusCode().is2xxSuccessful() ? contents.getBody() : null;
    }

    private String getNextUrl(final ResponseEntity<?> response) {
        return this.linkParser.parse(response.getHeaders().getFirst("Link")).get("next");
    }

}
