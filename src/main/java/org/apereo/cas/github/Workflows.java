package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class Workflows {

    private final long count;

    private final List<WorkflowRun> runs;

    @JsonCreator
    public Workflows(@JsonProperty("total_count") final long count,
                     @JsonProperty("workflow_runs") final List<WorkflowRun> runs) {
        this.count = count;
        this.runs = runs;
    }

    @Getter
    @ToString(of = {"runNumber", "name", "status", "event", "conclusion", "workflowId", "headRepository", "headBranch"}, includeFieldNames = false)
    public static class WorkflowRun {
        private final long id;

        private final String name;

        private final String event;

        private final String status;

        private final String conclusion;

        private final List<PullRequest> pullRequests;

        private final Repository repository;

        private final String cancelUrl;

        private final String headBranch;

        private final String headSha;

        private final long workflowId;

        private final Commit headCommit;

        private final Repository headRepository;

        private final long runNumber;

        private final OffsetDateTime creationTime;

        private final OffsetDateTime updatedTime;

        @JsonCreator
        public WorkflowRun(@JsonProperty("id") long id,
                           @JsonProperty("name") String name,
                           @JsonProperty("run_number") long runNumber,
                           @JsonProperty("event") String event,
                           @JsonProperty("status") String status,
                           @JsonProperty("conclusion") String conclusion,
                           @JsonProperty("cancel_url") String cancelUrl,
                           @JsonProperty("pull_requests") List<PullRequest> pullRequests,
                           @JsonProperty("repository") Repository repository,
                           @JsonProperty("head_branch") String headBranch,
                           @JsonProperty("head_sha") String headSha,
                           @JsonProperty("workflow_id") long workflowId,
                           @JsonProperty("head_commit") Commit headCommit,
                           @JsonProperty("created_at") final OffsetDateTime creationTime,
                           @JsonProperty("updated_at") final OffsetDateTime updatedTime,
                           @JsonProperty("head_repository") Repository headRepository) {
            this.id = id;
            this.name = name;
            this.event = event;
            this.status = status;
            this.conclusion = conclusion;
            this.pullRequests = pullRequests;
            this.repository = repository;
            this.cancelUrl = cancelUrl;
            this.headBranch = headBranch;
            this.runNumber = runNumber;
            this.headSha = headSha;
            this.workflowId = workflowId;
            this.headCommit = headCommit;
            this.headRepository = headRepository;
            this.creationTime = creationTime;
            this.updatedTime = updatedTime;
        }
    }
}
