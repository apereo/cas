package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link CheckRun}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
public class CheckRun {

    @RequiredArgsConstructor
    @Getter
    public enum Checks {
        BUILD_PULL_REQUEST("Build Pull Request");

        private final String name;

        public static boolean isAnyOf(final String name) {
            return Arrays.stream(Checks.values()).anyMatch(run -> run.getName().equalsIgnoreCase(name));
        }
    }
    
    private final long count;
    private final List<CheckRunDetails> runs;

    @JsonCreator
    public CheckRun(@JsonProperty("total_count") final long count,
                    @JsonProperty("check_runs") final List<CheckRunDetails> runs) {
        this.count = count;
        this.runs = runs;
    }

    public boolean hasCompletedCheckSuccessfully(final String name) {
        return this.runs.stream().anyMatch(r -> r.getName().equalsIgnoreCase(name)
            && r.getStatus().equalsIgnoreCase("success"));
    }

    @Getter
    @ToString(of = {"id", "name", "status"})
    public static class CheckRunDetails {
        private final String id;
        private final String status;
        private final String conclusion;
        private final String name;
        private final String description;
        private final String url;
        private final List<PullRequest> pullRequests;

        @JsonCreator
        public CheckRunDetails(@JsonProperty("status") final String status,
                               @JsonProperty("conclusion") final String conclusion,
                               @JsonProperty("url") final String url,
                               @JsonProperty("id") final String id,
                               @JsonProperty("name") final String name,
                               @JsonProperty("description") final String description,
                               @JsonProperty("pull_requests") final List<PullRequest> pullRequests) {
            this.status = status;
            this.url = url;
            this.id = id;
            this.description = description;
            this.name = name;
            this.pullRequests = pullRequests;
            this.conclusion = conclusion;
        }
    }
}
