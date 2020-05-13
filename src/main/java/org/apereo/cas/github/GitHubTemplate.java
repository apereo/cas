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

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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
public class GitHubTemplate implements GitHubOperations {

    private static final Logger log = LoggerFactory.getLogger(GitHubTemplate.class);

    private final RestOperations rest;

    private final LinkParser linkParser;

    public GitHubTemplate(final String token, final LinkParser linkParser) {
        this(createDefaultRestTemplate(token), linkParser);
    }

    GitHubTemplate(final RestOperations rest, final LinkParser linkParser) {
        this.rest = rest;
        this.linkParser = linkParser;
    }

    static RestTemplate createDefaultRestTemplate(final String token) {
        final RestTemplate rest = new RestTemplate();
        rest.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(final ClientHttpResponse response) throws IOException {
                if (response.getStatusCode() == HttpStatus.FORBIDDEN && response
                    .getHeaders().getFirst("X-RateLimit-Remaining").equals("0")) {
                    throw new IllegalStateException(
                        "Rate limit exceeded. Limit will reset at "
                            + new Date(Long
                            .valueOf(response.getHeaders().getFirst("X-RateLimit-Reset"))
                            * 1000));
                }
            }
        });
        final BufferingClientHttpRequestFactory bufferingClient = new BufferingClientHttpRequestFactory(
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
        val url = "https://api.github.com/repos/" + organization + '/' + repository
            + "/issues";
        return getPage(url, Issue[].class);
    }

    @Override
    public Page<PullRequest> getPullRequests(final String organization, final String repository) {
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/pulls?state=open";
        val headers = new LinkedMultiValueMap(Map.of("Accept", List.of("application/vnd.github.shadow-cat-preview+json")));
        return getPage(url, PullRequest[].class, Map.of(), headers);
    }

    @Override
    public PullRequest getPullRequest(final String organization, final String repository, final String number) {
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/pulls/" + number;
        val headers = new LinkedMultiValueMap(Map.of("Accept", List.of("application/vnd.github.shadow-cat-preview+json")));
        return getSinglePage(url, PullRequest.class, Map.of(), headers);
    }

    @Override
    public void closePullRequest(final String organization, final String repository, final String number) {
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/pulls/" + number;
        final URI uri = URI.create(url);
        log.info("Closing to pull request {}", uri);

        final Map<String, String> body = new HashMap<>();
        body.put("state", "closed");
        final ResponseEntity response = this.rest.exchange(new RequestEntity(body, HttpMethod.PATCH, uri), PullRequest.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to close to pull request. Response status: " + response.getStatusCode());
        }
    }

    @Override
    public Page<Comment> getComments(final Issue issue) {
        return getPage(issue.getCommentsUrl(), Comment[].class);
    }

    @Override
    public Page<Event> getEvents(final Issue issue) {
        return getPage(issue.getEventsUrl(), Event[].class);
    }

    @Override
    public Page<PullRequestFile> getPullRequestFiles(final String organization, final String repository, final String number) {
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/pulls/" + number + "/files";
        return getPage(url, PullRequestFile[].class);
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
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/commits/" + ref + "/status";
        return getSinglePage(url, CombinedCommitStatus.class);
    }

    @Override
    public Page<Commit> getPullRequestCommits(final String organization, final String repository, final String number) {
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/pulls/" + number + "/commits";
        return getPage(url, Commit[].class);
    }

    @Override
    public boolean mergeIntoBase(final String organization, final String repository, final PullRequest pr,
                                 final String commitTitle, final String commitMessage,
                                 final String shaToMatch, final String method) {
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/pulls/" + pr.getNumber() + "/merge";

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
    public PullRequest mergeWithBase(final String organization, final String repository, final PullRequest pr) {
        if (pr.getHead().getRepository().isFork()) {
            val url = "https://api.github.com/repos/" + organization + '/' + repository + "/pulls/" + pr.getNumber() + "/update-branch";
            final Map<String, String> params = new HashMap<>();
            params.put("expected_head_sha", pr.getHead().getSha());
            val responseEntity = this.rest.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(new LinkedMultiValueMap(Map.of("Accept", List.of("application/vnd.github.lydian-preview+json")))), Map.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Merged pull request {} with base", pr);
            } else {
                log.error("Unable to merge pulfl request {} with base {}", pr, responseEntity);
            }
            return pr;
        }

        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/merges";
        final URI uri = URI.create(url);
        final Map<String, String> body = new HashMap<>();
        val prBranch = pr.getHead().getRef();
        body.put("base", prBranch);

        val targetBranch = pr.getBase().getRef();
        body.put("head", targetBranch);

        body.put("commit_message", "Merged branch " + targetBranch + " into " + prBranch);

        val response = this.rest.exchange(new RequestEntity(body, HttpMethod.POST, uri), Map.class);
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            log.debug("Pull request [{}] already contains the [{}]; nothing to merge", targetBranch, pr);
        } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
            log.warn("Pull request [{}] has a merge conflict and cannot be merged with [{}]", pr, targetBranch);
        } else if (response.getStatusCode() == HttpStatus.CREATED) {
            log.info("Pull request [{}] is successfully merged with head [{}]", pr, targetBranch);
        } else {
            log.warn("Unable to handle merge with base; message [{}], status [{}]", response.getBody(), response.getStatusCode().getReasonPhrase());
        }
        return pr;
    }

    @Override
    public Page<Milestone> getMilestones(final String organization, final String name) {
        val url = "https://api.github.com/repos/" + organization + '/' + name + "/milestones?state=open";
        return getPage(url, Milestone[].class);
    }

    @Override
    public Page<Label> getLabels(final String organization, final String name) {
        val url = "https://api.github.com/repos/" + organization + '/' + name + "/labels";
        return getPage(url, Label[].class);
    }

    @Override
    public void setMilestone(final PullRequest pr, final Milestone milestone) {
        final URI uri = URI.create(pr.getMilestonesUrl());
        log.info("Adding milestone {} to pull request {}", milestone, uri);

        final Map<String, String> body = new HashMap<>();
        body.put("milestone", milestone.getNumber());

        final ResponseEntity response = this.rest.exchange(new RequestEntity(body, HttpMethod.PATCH, uri), PullRequest.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to add milestone to pull request. Response status: " + response.getStatusCode());
        }
    }

    private <T> Page<T> getPage(final String url, final Class<T[]> type, final Map params) {
        return getPage(url, type, params, new HttpHeaders());
    }

    private <T> Page<T> getPage(final String url, final Class<T[]> type, final Map params, final MultiValueMap headers) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        final HttpHeaders hd = new HttpHeaders(headers);
        final ResponseEntity<T> contents = this.rest.exchange(url, HttpMethod.GET, new HttpEntity<>(hd), type, params);
        List<T> body = Arrays.asList(type.cast(contents.getBody()));
        return new StandardPage<T>(body, () -> getPage(getNextUrl(contents), type));
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
        return contents.getBody();
    }

    private <T> T getSinglePage(final String url, final Class<T> type, final Map params, final MultiValueMap headers) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        val hd = new HttpHeaders(headers);
        final ResponseEntity<T> contents = this.rest.exchange(url, HttpMethod.GET, new HttpEntity<>(hd), type, params);
        return contents.getBody();
    }

    private String getNextUrl(final ResponseEntity<?> response) {
        return this.linkParser.parse(response.getHeaders().getFirst("Link")).get("next");
    }

    @Override
    public PullRequest addLabel(final PullRequest pr, final String label) {
        val uri = URI.create(pr.getLabelsUrl());
        log.info("Adding label {} to pull request {}", label, pr);
        final ResponseEntity<Label[]> response = this.rest.exchange(
            new RequestEntity<>(Arrays.asList(label), HttpMethod.POST, uri),
            Label[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to add label to pull request. Response status: " + response.getStatusCode());
        }
        return pr;
    }

    @Override
    public Issue addLabel(final Issue issue, final String labelName) {
        final URI uri = URI.create(issue.getLabelsUrl().replace("{/name}", ""));
        log.info("Adding label {} to {}", labelName, uri);
        final ResponseEntity<Label[]> response = this.rest.exchange(
            new RequestEntity<>(Arrays.asList(labelName), HttpMethod.POST, uri),
            Label[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to add label to issue. Response status: "
                + response.getStatusCode());
        }
        return new Issue(issue.getUrl(), issue.getCommentsUrl(), issue.getEventsUrl(),
            issue.getLabelsUrl(), issue.getUser(), Arrays.asList(response.getBody()),
            issue.getMilestone(), issue.getPullRequest());
    }

    @Override
    public void removeLabel(final PullRequest pullRequest, final String label) {
        final String encodedName;
        try {
            encodedName = new URI(null, null, label, null).toString();
        } catch (final URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        log.info("Removing label {} from pull request {}", label, pullRequest);
        val url = pullRequest.getLabelsUrl().replace("{/name}", '/' + encodedName);
        final ResponseEntity<Label[]> response = this.rest.exchange(
            new RequestEntity<Void>(HttpMethod.DELETE, URI.create(url)), Label[].class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Failed to remove label from pull request. Response status: "
                + response.getStatusCode());
        }
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
        final ResponseEntity<Label[]> response = this.rest.exchange(
            new RequestEntity<Void>(HttpMethod.DELETE, URI.create(
                issue.getLabelsUrl().replace("{/name}", '/' + encodedName))),
            Label[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to remove label from issue. Response status: "
                + response.getStatusCode());
        }
        return new Issue(issue.getUrl(), issue.getCommentsUrl(), issue.getEventsUrl(),
            issue.getLabelsUrl(), issue.getUser(), Arrays.asList(response.getBody()),
            issue.getMilestone(), issue.getPullRequest());
    }

    @Override
    public Comment addComment(final Issue issue, final String comment) {
        final Map<String, String> body = new HashMap<>();
        body.put("body", comment);
        return this.rest.postForEntity(issue.getCommentsUrl(), body, Comment.class).getBody();
    }

    @Override
    public Comment addComment(final PullRequest pullRequest, final String comment) {
        final Map<String, String> body = new HashMap<>();
        body.put("body", comment);
        return this.rest.postForEntity(pullRequest.getCommentsUrl(), body, Comment.class).getBody();
    }

    @Override
    public Issue close(final Issue issue) {
        final Map<String, String> body = new HashMap<>();
        body.put("state", "closed");
        final ResponseEntity<Issue> response = this.rest.exchange(
            new RequestEntity<>(body, HttpMethod.PATCH, URI.create(issue.getUrl())),
            Issue.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to close issue. Response status: "
                + response.getStatusCode());
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
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/check-runs";

        val headers = new LinkedMultiValueMap(Map.of("Accept", List.of("application/vnd.github.antiope-preview+json")));
        val response = this.rest.exchange(new RequestEntity(body, headers, HttpMethod.POST, new URI(url)), Map.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public CheckRun getCheckRunsFor(final String organization, final String repository, final String ref,
                                    final String checkName, final String status, final String filter) {
        final Map<String, String> params = new HashMap<>();
        if (StringUtils.hasText(checkName)) {
            params.put("check_name", checkName);
        }
        if (StringUtils.hasText(status)) {
            params.put("status", status);
        }
        if (StringUtils.hasText(filter)) {
            params.put("filter", filter);
        }
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/commits/" + ref + "/check-runs";
        return getSinglePage(url, CheckRun.class, params,
            new LinkedMultiValueMap(Map.of("Accept", List.of("application/vnd.github.antiope-preview+json"))));
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
        val url = "https://api.github.com/repos/" + organization + '/' + repository + "/statuses/" + ref;
        val response = this.rest.exchange(new RequestEntity(body, HttpMethod.POST, new URI(url)), Map.class);
        return response.getStatusCode().is2xxSuccessful();

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

}
