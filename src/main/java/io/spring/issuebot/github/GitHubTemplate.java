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

package io.spring.issuebot.github;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * Central class for interacting with GitHub's REST API.
 *
 * @author Andy Wilkinson
 */
public class GitHubTemplate implements GitHubOperations {

	private static final Logger log = LoggerFactory.getLogger(GitHubTemplate.class);

	private final RestOperations rest;

	private final LinkParser linkParser;

	/**
	 * Creates a new {@code GitHubTemplate} that will use the given {@code username} and
	 * {@code password} to authenticate, and the given {@code linkParser} to parse links
	 * from responses' {@code Link} header.
	 *
	 * @param username the username
	 * @param password the password
	 * @param linkParser the link parser
	 */
	public GitHubTemplate(String username, String password, LinkParser linkParser) {
		this(createDefaultRestTemplate(username, password), linkParser);
	}

	GitHubTemplate(RestOperations rest, LinkParser linkParser) {
		this.rest = rest;
		this.linkParser = linkParser;
	}

	RestOperations getRestOperations() {
		return this.rest;
	}

	static RestTemplate createDefaultRestTemplate(String username, String password) {
		RestTemplate rest = new RestTemplate();
		rest.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
				if (response.getStatusCode() == HttpStatus.FORBIDDEN && response
						.getHeaders().getFirst("X-RateLimit-Remaining").equals("0")) {
					throw new IllegalStateException(
							"Rate limit exceeded. Limit will reset at "
									+ new Date(Long
											.valueOf(response.getHeaders()
													.getFirst("X-RateLimit-Reset"))
									* 1000));
				}
			}
		});
		rest.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		rest.setInterceptors(Collections
				.singletonList(new BasicAuthorizationInterceptor(username, password)));
		return rest;
	}

	@Override
	public Page<Issue> getIssues(String organization, String repository) {
		String url = "https://api.github.com/repos/" + organization + "/" + repository
				+ "/issues";
		return getIssues(url);
	}

	private Page<Issue> getIssues(String url) {
		if (!StringUtils.hasText(url)) {
			return null;
		}
		ResponseEntity<Issue[]> issues = this.rest.getForEntity(url, Issue[].class);
		return new StandardPage<Issue>(Arrays.asList(issues.getBody()),
				() -> getIssues(getNextUrl(issues)));
	}

	private String getNextUrl(ResponseEntity<?> response) {
		return this.linkParser.parse(response.getHeaders().getFirst("Link")).get("next");
	}

	@Override
	public Page<Comment> getComments(Issue issue) {
		return getComments(issue.getCommentsUrl());
	}

	private Page<Comment> getComments(String url) {
		if (!StringUtils.hasText(url)) {
			return null;
		}
		ResponseEntity<Comment[]> comments = this.rest.getForEntity(url, Comment[].class);
		return new StandardPage<Comment>(Arrays.asList(comments.getBody()),
				() -> getComments(getNextUrl(comments)));
	}

	@Override
	public Issue addLabel(Issue issue, String labelName) {
		Map<String, Object> body = new HashMap<>();
		body.put("labels", Arrays.asList(labelName));
		ResponseEntity<Issue> exchange = this.rest.exchange(
				new RequestEntity<>(body, HttpMethod.POST, URI.create(issue.getUrl())),
				Issue.class);
		if (exchange.getStatusCode() != HttpStatus.OK) {
			log.warn("Failed to add label to issue. Response status: "
					+ exchange.getStatusCode());
		}
		return exchange.getBody();
	}

	private static class BasicAuthorizationInterceptor
			implements ClientHttpRequestInterceptor {

		private static final Charset UTF_8 = Charset.forName("UTF-8");

		private final String username;

		private final String password;

		BasicAuthorizationInterceptor(String username, String password) {
			this.username = username;
			this.password = (password == null ? "" : password);
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body,
				ClientHttpRequestExecution execution) throws IOException {
			String token = Base64Utils.encodeToString(
					(this.username + ":" + this.password).getBytes(UTF_8));
			request.getHeaders().add("Authorization", "Basic " + token);
			return execution.execute(request, body);
		}

	}

}
