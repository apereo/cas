package org.apereo.cas.controller;

import org.apereo.cas.CasLabels;
import org.apereo.cas.MonitoredRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@Slf4j
public class RepositoryController {

    @Autowired
    @Qualifier("monitoredRepository")
    private MonitoredRepository repository;

    @Autowired
    @Qualifier("stagingRepository")
    private ObjectProvider<MonitoredRepository> stagingRepository;

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public Map<String, String> home() {
        var map = new LinkedHashMap<String, String>();
        try {
            map.put("name", repository.getOrganization() + '/' + repository.getName());
            map.put("repository", repository.getGitHubProperties().getRepository().getUrl());
            map.put("version", repository.getCurrentVersionInMaster().toString());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
        return map;
    }

    @PostMapping(value = "/repo/pulls/{prNumber}/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity listPullRequestCommits(@PathVariable final String prNumber) throws Exception {
        val pullRequest = repository.getPullRequest(prNumber);
        if (pullRequest == null) {
            return ResponseEntity.notFound().build();
        }
        val verified = repository.verifyPullRequest(pullRequest);
        return ResponseEntity.ok(Map.of("verified", verified));
    }

    @PostMapping(value = "/repo/workflows/rerun", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity rerunWorkflows() throws Exception {
        return ResponseEntity.ok(repository.rerunFailedWorkflowJobs());
    }

    @PostMapping(value = "/repo/workflows/clean", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity cleanWorkflows() throws Exception {
        repository.removeCancelledWorkflowRuns();
        repository.removePullRequestWorkflowRunsForMissingBranches();
        repository.removeOldWorkflowRuns();

        stagingRepository.ifAvailable(repository -> {
            repository.removeCancelledWorkflowRuns();
            repository.removePullRequestWorkflowRunsForMissingBranches();
            repository.removeOldWorkflowRuns();
        });
        return ResponseEntity.ok().build();
    }

    
    @PostMapping(value = "/repo/pulls/{prNumber}/comments/clean", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity cleanComments(@PathVariable final String prNumber) throws Exception {
        val pullRequest = repository.getPullRequest(prNumber);
        if (pullRequest == null) {
            return ResponseEntity.notFound().build();
        }
        repository.removeAllApereoCasBotCommentsFrom(pullRequest);
        repository.removeLabelFrom(pullRequest, CasLabels.LABEL_PENDING_PORT_FORWARD);
        repository.removeLabelFrom(pullRequest, CasLabels.LABEL_SEE_CONTRIBUTOR_GUIDELINES);
        repository.removeLabelFrom(pullRequest, CasLabels.LABEL_PROPOSAL_DECLINED);
        repository.removeLabelFrom(pullRequest, CasLabels.LABEL_PENDING_NEEDS_TESTS);
        repository.labelPullRequestAs(pullRequest, CasLabels.LABEL_UNDER_REVIEW);
        
        if (pullRequest.isClosed()) {
            repository.open(pullRequest);
        }
        if (pullRequest.isBot()) {
            repository.labelPullRequestAs(pullRequest, CasLabels.LABEL_SKIP_CI);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/repo/pulls/{prNumber}/close", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity closePullRequest(@PathVariable final String prNumber) throws Exception {
        val pullRequest = repository.getPullRequest(prNumber);
        if (pullRequest == null) {
            return ResponseEntity.notFound().build();
        }
        repository.removeAllApereoCasBotCommentsFrom(pullRequest);
        repository.labelPullRequestAs(pullRequest, CasLabels.LABEL_SEE_CONTRIBUTOR_GUIDELINES);
        repository.labelPullRequestAs(pullRequest, CasLabels.LABEL_PROPOSAL_DECLINED);
        repository.close(pullRequest);
        return ResponseEntity.ok(pullRequest);
    }


    @GetMapping(value = "/repo/pulls", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public List listPulls() {
        return repository.getOpenPullRequests()
            .stream()
            .map(pr -> {
                val map = new LinkedHashMap<String, String>();
                map.put("number", pr.getNumber());
                map.put("title", pr.getTitle());
                map.put("branch", pr.getBase().getLabel());
                map.put("author", pr.getUser().getLogin());
                return map;
            })
            .collect(Collectors.toList());
    }

    @PostMapping(value = "/repo/pulls/{prNumber}/labels/automerge", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity automerge(@PathVariable final String prNumber) {
        var pullRequest = repository.getPullRequest(prNumber);
        if (pullRequest == null) {
            return ResponseEntity.notFound().build();
        }
        if (pullRequest.isLocked()) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
        if (pullRequest.isClosed()) {
            repository.open(pullRequest);
        }
        repository.removeAllApereoCasBotCommentsFrom(pullRequest);
        repository.removeLabelFrom(pullRequest, CasLabels.LABEL_PENDING_PORT_FORWARD);
        repository.removeLabelFrom(pullRequest, CasLabels.LABEL_SEE_CONTRIBUTOR_GUIDELINES);
        repository.removeLabelFrom(pullRequest, CasLabels.LABEL_PROPOSAL_DECLINED);
        repository.labelPullRequestAs(pullRequest, CasLabels.LABEL_AUTO_MERGE);
        repository.labelPullRequestAs(pullRequest, CasLabels.LABEL_UNDER_REVIEW);
        if (pullRequest.isBot()) {
            repository.labelPullRequestAs(pullRequest, CasLabels.LABEL_SKIP_CI);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/repo/pulls/{prNumber}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity approve(@PathVariable final String prNumber) {
        val pullRequest = repository.getPullRequest(prNumber);
        if (pullRequest == null) {
            return ResponseEntity.notFound().build();
        }
        if (pullRequest.isLocked()) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
        return ResponseEntity.ok(Map.of("approved",
            repository.approvePullRequest(pullRequest, false)));
    }

    @PostMapping(value = "/repo/pulls/{prNumber}/labels/runci", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity runci(@PathVariable final String prNumber) {
        val pullRequest = repository.getPullRequest(prNumber);
        if (pullRequest == null) {
            return ResponseEntity.notFound().build();
        }
        if (pullRequest.isLocked() || pullRequest.isDraft() || pullRequest.isWorkInProgress()) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
        if (pullRequest.isLabeledAs(CasLabels.LABEL_CI)) {
            repository.removeLabelFrom(pullRequest, CasLabels.LABEL_CI);
        }
        repository.labelPullRequestAs(pullRequest, CasLabels.LABEL_CI);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/repo/pulls/{prNumber}/stale", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity stalePullRequest(@PathVariable final String prNumber) {
        val pullRequest = repository.getPullRequest(prNumber);
        if (pullRequest == null) {
            return ResponseEntity.notFound().build();
        }
        if (pullRequest.isLocked()) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        }
        if (pullRequest.isLabeledAs(CasLabels.LABEL_CI)) {
            repository.removeLabelFrom(pullRequest, CasLabels.LABEL_CI);
        }
        repository.closeStalePullRequest(pullRequest);
        return ResponseEntity.ok().build();
    }
}
