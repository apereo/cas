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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link GitHubTemplate}.
 *
 * @author Andy Wilkinson
 */
public class GitHubTemplateTests {

    private final RestTemplate rest = GitHubTemplate.createDefaultRestTemplate("username",
        "password");

    private final MockRestServiceServer server = MockRestServiceServer
        .createServer(this.rest);

    private GitHubTemplate gitHub = new GitHubTemplate(this.rest, new RegexLinkParser());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void noIssues() {
        this.server.expect(requestTo("https://api.github.com/repos/org/repo/issues"))
            .andExpect(method(HttpMethod.GET)).andExpect(basicAuth())
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        Page<Issue> issues = this.gitHub.getIssues("org", "repo");
        assertThat(issues.getContent().size(), is(0));
        assertThat(issues.next(), is(nullValue()));
    }

    @Test
    public void singlePageOfIssues() {
        this.server.expect(requestTo("https://api.github.com/repos/org/repo/issues"))
            .andExpect(method(HttpMethod.GET)).andExpect(basicAuth())
            .andRespond(withResource("issues-page-one.json"));
        Page<Issue> issues = this.gitHub.getIssues("org", "repo");
        assertThat(issues.getContent().size(), is(15));
        assertThat(issues.next(), is(nullValue()));
    }

    @Test
    public void multiplePagesOfIssues() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Link", "<page-two>; rel=\"next\"");
        this.server.expect(requestTo("https://api.github.com/repos/org/repo/issues"))
            .andExpect(method(HttpMethod.GET)).andExpect(basicAuth())
            .andRespond(withResource("issues-page-one.json",
                "Link:<page-two>; rel=\"next\""));
        this.server.expect(requestTo("page-two")).andExpect(method(HttpMethod.GET))
            .andExpect(basicAuth()).andRespond(withResource("issues-page-two.json"));
        Page<Issue> pageOne = this.gitHub.getIssues("org", "repo");
        assertThat(pageOne.getContent().size(), is(15));
        Page<Issue> pageTwo = pageOne.next();
        assertThat(pageTwo, is(not(nullValue())));
        assertThat(pageTwo.getContent().size(), is(15));
    }

    @Test
    public void rateLimited() {
        long reset = System.currentTimeMillis();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RateLimit-Remaining", "0");
        headers.set("X-RateLimit-Reset", Long.toString(reset / 1000));
        this.server.expect(requestTo("https://api.github.com/repos/org/repo/issues"))
            .andExpect(method(HttpMethod.GET)).andExpect(basicAuth())
            .andRespond(withStatus(HttpStatus.FORBIDDEN).headers(headers));
        this.thrown.expect(IllegalStateException.class);
        this.thrown.expectMessage(equalToIgnoringCase(
            "Rate limit exceeded. Limit will reset at " + new Date(reset)));
        this.gitHub.getIssues("org", "repo");
    }

    @Test
    public void noComments() {
        this.server.expect(requestTo("commentsUrl")).andExpect(method(HttpMethod.GET))
            .andExpect(basicAuth())
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        Page<Comment> comments = this.gitHub.getComments(
            new Issue(null, "commentsUrl", null, null, null, null, null, null));
        assertThat(comments.getContent().size(), is(0));
        assertThat(comments.next(), is(nullValue()));
    }

    @Test
    public void singlePageOfComments() {
        this.server.expect(requestTo("commentsUrl")).andExpect(method(HttpMethod.GET))
            .andExpect(basicAuth())
            .andRespond(withResource("comments-page-one.json"));
        Page<Comment> comments = this.gitHub.getComments(
            new Issue(null, "commentsUrl", null, null, null, null, null, null));
        assertThat(comments.getContent().size(), is(17));
        assertThat(comments.next(), is(nullValue()));
    }

    @Test
    public void multiplePagesOfComments() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Link", "<page-two>; rel=\"next\"");
        this.server.expect(requestTo("commentsUrl")).andExpect(method(HttpMethod.GET))
            .andExpect(basicAuth()).andRespond(withResource("comments-page-one.json",
            "Link:<page-two>; rel=\"next\""));
        this.server.expect(requestTo("page-two")).andExpect(method(HttpMethod.GET))
            .andExpect(basicAuth())
            .andRespond(withResource("comments-page-two.json"));
        Page<Comment> pageOne = this.gitHub.getComments(
            new Issue(null, "commentsUrl", null, null, null, null, null, null));
        assertThat(pageOne.getContent().size(), is(17));
        Page<Comment> pageTwo = pageOne.next();
        assertThat(pageTwo, is(not(nullValue())));
        assertThat(pageTwo.getContent().size(), is(3));
    }

    @Test
    public void addLabelToIssue() {
        this.server.expect(requestTo("labelsUrl")).andExpect(method(HttpMethod.POST))
            .andExpect(basicAuth()).andExpect(content().string("[\"test\"]"))
            .andRespond(
                withSuccess("[{\"name\":\"test\"}]", MediaType.APPLICATION_JSON));
        Issue issue = new Issue("issueUrl", null, null, "labelsUrl{/name}", null, null,
            null, null);
        Issue modifiedIssue = this.gitHub.addLabel(issue, "test");
        assertThat(modifiedIssue.getLabels(), hasSize(1));
    }

    @Test
    public void removeLabelFromIssue() {
        this.server.expect(requestTo("labels/test")).andExpect(method(HttpMethod.DELETE))
            .andExpect(basicAuth())
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        Issue issue = new Issue(null, null, null, "labels{/name}", null, null, null,
            null);
        Issue modifiedIssue = this.gitHub.removeLabel(issue, "test");
        assertThat(modifiedIssue.getLabels(), hasSize(0));
    }

    @Test
    public void removeLabelWithNameThatRequiresEncodingFromIssue() {
        this.server.expect(requestTo("labels/status:%20foo")).andExpect(method(HttpMethod.DELETE))
            .andExpect(basicAuth())
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        Issue issue = new Issue(null, null, null, "labels{/name}", null, null, null,
            null);
        Issue modifiedIssue = this.gitHub.removeLabel(issue, "status: foo");
        assertThat(modifiedIssue.getLabels(), hasSize(0));
    }

    @Test
    public void addCommentToIssue() {
        this.server.expect(requestTo("commentsUrl")).andExpect(method(HttpMethod.POST))
            .andExpect(basicAuth())
            .andExpect(content().string("{\"body\":\"A test comment\"}"))
            .andRespond(withResource("new-comment.json"));
        Issue issue = new Issue(null, "commentsUrl", null, null, null, null, null, null);
        Comment comment = this.gitHub.addComment(issue, "A test comment");
        assertThat(comment, is(not(nullValue())));
    }

    @Test
    public void singlePageOfEvents() {
        this.server.expect(requestTo("eventsUrl")).andExpect(method(HttpMethod.GET))
            .andExpect(basicAuth()).andRespond(withResource("events-page-one.json"));
        Page<Event> events = this.gitHub.getEvents(
            new Issue(null, null, "eventsUrl", null, null, null, null, null));
        assertThat(events.getContent().size(), is(12));
        assertThat(events.next(), is(nullValue()));
    }

    @Test
    public void multiplePagesOfEvents() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Link", "<page-two>; rel=\"next\"");
        this.server.expect(requestTo("eventsUrl")).andExpect(method(HttpMethod.GET))
            .andExpect(basicAuth()).andRespond(withResource("events-page-one.json",
            "Link:<page-two>; rel=\"next\""));
        this.server.expect(requestTo("page-two")).andExpect(method(HttpMethod.GET))
            .andExpect(basicAuth()).andRespond(withResource("events-page-two.json"));
        Page<Event> pageOne = this.gitHub.getEvents(
            new Issue(null, null, "eventsUrl", null, null, null, null, null));
        assertThat(pageOne.getContent().size(), is(12));
        Page<Event> pageTwo = pageOne.next();
        assertThat(pageTwo, is(not(nullValue())));
        assertThat(pageTwo.getContent().size(), is(3));
    }

    @Test
    public void closeIssue() {
        this.server.expect(requestTo("issueUrl")).andExpect(method(HttpMethod.PATCH))
            .andExpect(basicAuth())
            .andExpect(content().string("{\"state\":\"closed\"}"))
            .andRespond(withSuccess("{\"url\":\"updatedIssueUrl\"}",
                MediaType.APPLICATION_JSON));
        Issue closedIssue = this.gitHub
            .close(new Issue("issueUrl", null, null, null, null, null, null, null));
        assertThat(closedIssue.getUrl(), is(equalTo("updatedIssueUrl")));
    }

    private DefaultResponseCreator withResource(String resource, String... headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        for (String header : headers) {
            String[] components = header.split(":");
            httpHeaders.set(components[0], components[1]);
        }
        return withSuccess(new UrlResource(getClass().getResource(resource)),
            MediaType.APPLICATION_JSON).headers(httpHeaders);
    }

    private RequestMatcher basicAuth() {
        return header("Authorization", "Basic "
            + new String(Base64Utils.encode("username:password".getBytes())));
    }

}
