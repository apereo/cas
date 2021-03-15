package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
public class WorkflowRun {

    private final long count;

    private final List<WorkflowRunDetails> runs;

    @JsonCreator
    public WorkflowRun(@JsonProperty("total_count") final long count,
                       @JsonProperty("workflow_runs") final List<WorkflowRunDetails> runs) {
        this.count = count;
        this.runs = runs;
    }

    @Getter
    @ToString(of = {"id", "name", "status", "event", "workflowId", "headRepository", "headBranch"}, includeFieldNames = false)
    public static class WorkflowRunDetails {
        private String id;

        private String name;

        private String event;

        private String status;

        private List<PullRequest> pullRequests;

        private Repository repository;

        private String cancelUrl;

        private String headBranch;

        private String headSha;

        private String workflowId;

        private Commit headCommit;

        private Repository headRepository;

        @JsonCreator
        public WorkflowRunDetails(@JsonProperty("id") String id,
                                  @JsonProperty("name") String name,
                                  @JsonProperty("event") String event,
                                  @JsonProperty("status") String status,
                                  @JsonProperty("cancel_url") String cancelUrl,
                                  @JsonProperty("pull_requests") List<PullRequest> pullRequests,
                                  @JsonProperty("repository") Repository repository,
                                  @JsonProperty("head_branch") String headBranch,
                                  @JsonProperty("head_sha") String headSha,
                                  @JsonProperty("workflow_id") String workflowId,
                                  @JsonProperty("head_commit") Commit headCommit,
                                  @JsonProperty("head_repository") Repository headRepository) {
            this.id = id;
            this.name = name;
            this.event = event;
            this.status = status;
            this.pullRequests = pullRequests;
            this.repository = repository;
            this.cancelUrl = cancelUrl;
            this.headBranch = headBranch;
            this.headSha = headSha;
            this.workflowId = workflowId;
            this.headCommit = headCommit;
            this.headRepository = headRepository;
        }
    }
}
