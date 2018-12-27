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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public final class Commit {
    private String url;
    private String sha;
    private User author;
    private User committer;
    private GitCommit commit;

    @JsonCreator
    public Commit(@JsonProperty("url") final String url,
                  @JsonProperty("sha") final String sha,
                  @JsonProperty("author") final User author,
                  @JsonProperty("committer") final User committer,
                  @JsonProperty("commit") final GitCommit commit) {
        this.url = url;
        this.sha = sha;
        this.author = author;
        this.committer = committer;
        this.commit = commit;
    }

    @Getter
    public static class GitCommit {
        private String url;
        private String message;

        @JsonCreator
        public GitCommit(@JsonProperty("url") final String url,
                         @JsonProperty("message") final String message) {
            this.url = url;
            this.message = message;
        }
    }
}
