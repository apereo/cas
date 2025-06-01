package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
public class Workflows {

    private final long count;

    private final List<WorkflowRun> runs;

    @RequiredArgsConstructor
    @Getter
    public enum WorkflowRunStatus {
        QUEUED("queued"),
        WAITING("waiting"),
        CANCELLED("cancelled"),
        IN_PROGRESS("in progress"),
        SKIPPED("skipped"),
        SUCCESS("success"),
        FAILURE("failure"),
        STALE("stale"),
        COMPLETED("completed");

        private final String name;

        public static Optional<WorkflowRunStatus> from(final WorkflowRun run) {
            return Arrays.stream(values()).filter(name -> run.getStatus().equalsIgnoreCase(name.getName())).findFirst();
        }
    }

    @RequiredArgsConstructor
    @Getter
    public enum WorkflowRunEvent {
        PULL_REQUEST("pull_request"),
        PUSH("push"),
        DYNAMIC("dynamic"),
        SCHEDULE("schedule");

        private final String name;

        public static Optional<WorkflowRunEvent> from(final WorkflowRun run) {
            return Arrays.stream(values()).filter(name -> run.getStatus().equalsIgnoreCase(name.getName())).findFirst();
        }
    }
    
    @JsonCreator
    public Workflows(@JsonProperty("total_count") final long count,
                     @JsonProperty("workflow_runs") final List<WorkflowRun> runs) {
        this.count = count;
        this.runs = runs;
    }


    @Getter
    @ToString(of = {"id", "name", "headSha", "displayTitle"}, includeFieldNames = false)
    public static class WorkflowRunAttempt {
        private final long id;
        private final long workflowId;
        private final long runNumber;
        private final String name;
        private final String event;
        private final String displayTitle;
        private final String status;
        private final String conclusion;
        private final String headBranch;
        private final String headSha;
        private final User actor;
        private final HeadCommit headCommit;
        private final Repository repository;
        private final Repository headRepository;

        @JsonCreator
        public WorkflowRunAttempt(@JsonProperty("id") long id,
                           @JsonProperty("name") String name,
                           @JsonProperty("run_number") long runNumber,
                           @JsonProperty("event") String event,
                           @JsonProperty("status") String status,
                           @JsonProperty("conclusion") String conclusion,
                           @JsonProperty("repository") Repository repository,
                           @JsonProperty("head_branch") String headBranch,
                           @JsonProperty("display_title") String displayTitle,
                           @JsonProperty("head_sha") String headSha,
                           @JsonProperty("workflow_id") long workflowId,
                           @JsonProperty("actor") User actor,
                           @JsonProperty("head_commit") HeadCommit headCommit,
                           @JsonProperty("head_repository") Repository headRepository) {
            this.id = id;
            this.name = name;
            this.event = event;
            this.status = status;
            this.conclusion = conclusion;
            this.repository = repository;
            this.headBranch = headBranch;
            this.runNumber = runNumber;
            this.headSha = headSha;
            this.workflowId = workflowId;
            this.headRepository = headRepository;
            this.headCommit = headCommit;
            this.actor = actor;
            this.displayTitle = displayTitle;
        }
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

        private final HeadCommit headCommit;

        private final Repository headRepository;

        private final long runNumber;

        private final OffsetDateTime creationTime;

        private final OffsetDateTime updatedTime;

        private final long runAttempt;

        private final User actor;

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
                           @JsonProperty("run_attempt") long runAttempt,
                           @JsonProperty("actor") User actor,
                           @JsonProperty("head_commit") HeadCommit headCommit,
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
            this.runAttempt = runAttempt;
            this.actor = actor;
        }

        public boolean isConcludedSuccessfully() {
            return Workflows.WorkflowRunStatus.COMPLETED.getName().equalsIgnoreCase(status)
                && "success".equalsIgnoreCase(conclusion);
        }

        public boolean isRemovable() {
            return Workflows.WorkflowRunEvent.PUSH.getName().equalsIgnoreCase(getEvent())
                || Workflows.WorkflowRunEvent.DYNAMIC.getName().equalsIgnoreCase(getEvent())
                || Workflows.WorkflowRunEvent.SCHEDULE.getName().equalsIgnoreCase(getEvent());
        }

        public boolean isSkipped() {
            return WorkflowRunStatus.COMPLETED.getName().equalsIgnoreCase(status)
                   && "skipped".equalsIgnoreCase(conclusion);
        }
    }
}
