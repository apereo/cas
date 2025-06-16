package org.apereo.cas;

import com.github.zafarkhaja.semver.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.github.Base;
import org.apereo.cas.github.Branch;
import org.apereo.cas.github.CheckRun;
import org.apereo.cas.github.CombinedCommitStatus;
import org.apereo.cas.github.Comment;
import org.apereo.cas.github.Commit;
import org.apereo.cas.github.CommitStatus;
import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.Label;
import org.apereo.cas.github.Milestone;
import org.apereo.cas.github.PullRequest;
import org.apereo.cas.github.PullRequestFile;
import org.apereo.cas.github.PullRequestReview;
import org.apereo.cas.github.TimelineEntry;
import org.apereo.cas.github.Workflows;
import org.semver4j.Semver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
@Slf4j
public class MonitoredRepository {
    private final GitHubOperations gitHub;

    private final GitHubProperties.Repository repositoryProperties;
    private final GitHubProperties gitHubProperties;

    private Version currentVersionInMaster;

    public boolean approvePullRequest(final PullRequest pr, final boolean includeComment) {
        return gitHub.approve(getOrganization(), getName(), pr, includeComment);
    }

    public List<PullRequest> getOpenPullRequests() {
        final List<PullRequest> pullRequests = new ArrayList<>();
        try {
            var page = gitHub.getPullRequests(getOrganization(), getName());
            while (page != null) {
                pullRequests.addAll(page.getContent());
                page = page.next();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return pullRequests;
    }

    public Workflows getSuccessfulWorkflowRunsFor(final Base base, final Commit first) {
        return gitHub.getWorkflowRuns(base.getRepository().getOwner().getLogin(),
                base.getRepository().getName(), first, Workflows.WorkflowRunStatus.SUCCESS);
    }

    public Boolean verifyPullRequest(final PullRequest pullRequest) {
        try {
            removeAllApereoCasBotCommentsFrom(pullRequest);
            val mostRecentCommit = getPullRequestCommits(pullRequest)
                    .stream()
                    .filter(c -> !c.getCommit().isMergeCommit())
                    .toList()
                    .getFirst();
            val workflowRuns = getSuccessfulWorkflowRunsFor(pullRequest.getHead(), mostRecentCommit);
            var matchFound = true;
            var missingRuns = new ArrayList<String>();
            if (workflowRuns.getRuns().stream().noneMatch(run -> run.getName().equalsIgnoreCase(WorkflowRuns.CODE_ANALYSIS.getName()))) {
                matchFound = false;
                missingRuns.add(WorkflowRuns.CODE_ANALYSIS.getName());
            }
            if (workflowRuns.getRuns().stream().noneMatch(run -> run.getName().equalsIgnoreCase(WorkflowRuns.VALIDATION.getName()))) {
                matchFound = false;
                missingRuns.add(WorkflowRuns.VALIDATION.getName());
            }
            if (workflowRuns.getRuns().stream().noneMatch(run -> run.getName().equalsIgnoreCase(WorkflowRuns.FUNCTIONAL_TESTS.getName()))) {
                matchFound = false;
                missingRuns.add(WorkflowRuns.FUNCTIONAL_TESTS.getName());
            }
            if (workflowRuns.getRuns().stream().noneMatch(run -> run.getName().equalsIgnoreCase(WorkflowRuns.UNIT_TESTS.getName()))) {
                matchFound = false;
                missingRuns.add(WorkflowRuns.UNIT_TESTS.getName());
            }

            if (!matchFound) {
                var template = IOUtils.toString(new ClassPathResource("template-run-tests.md").getInputStream(), StandardCharsets.UTF_8);
                template = template.replace("${commitId}", mostRecentCommit.getSha());
                template = template.replace("${forkedRepository}", pullRequest.getHead().getRepository().getHtmlUrl());
                template = template.replace("${link}", Memes.NO_TESTS.select());
                template = template.replace("${branch}", pullRequest.getHead().getRef());
                template = template.replace("${missingRuns}", String.join(",", missingRuns));

                labelPullRequestAs(pullRequest, CasLabels.LABEL_PENDING_NEEDS_TESTS);
                labelPullRequestAs(pullRequest, CasLabels.LABEL_WIP);
                addComment(pullRequest, template);
                return false;
            }
            removeLabelFrom(pullRequest, CasLabels.LABEL_PENDING_NEEDS_TESTS);
            removeLabelFrom(pullRequest, CasLabels.LABEL_WIP);
            addComment(pullRequest, "Pull request is now verified. Nicely done! :thumbsup: :rocket: ");
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public Optional<DependencyRange> extractBotDependencyRange(final PullRequest pr) {
        if (pr.getTitle().startsWith("chore(deps): bump") && pr.getTitle().endsWith("-SNAPSHOT")) {
            log.debug("Ignoring SNAPSHOT dependency upgrade {}", pr);
            labelPullRequestAs(pr, CasLabels.LABEL_PROPOSAL_DECLINED);
            close(pr);
            return Optional.empty();
        }
        if (pr.getTitle().startsWith("Bump") && pr.getTitle().matches("Bump.+from.+-M[0-9]+")) {
            log.debug("Ignoring Milestone dependency upgrade {}", pr);
            return Optional.empty();
        }

        var pattern = Pattern.compile("`(\\d+\\.\\d+\\.\\d+).*` \\-\\> `(\\d+\\.\\d+\\.\\d+).*`");
        var matcher = pattern.matcher(pr.getBody());
        if (matcher.find()) {
            var startingVersion = new Semver(matcher.group(1));
            var endingVersion = new Semver(matcher.group(2));
            return Optional.of(new DependencyRange(startingVersion, endingVersion));
        }

        pattern = Pattern.compile("`(\\d+\\.\\d+(\\.\\d+)*).*` \\-\\> `(\\d+\\.\\d+(\\.\\d+)*).*`");
        matcher = pattern.matcher(pr.getBody());
        if (matcher.find()) {
            var startingVersion = Semver.coerce(matcher.group(1));
            var endingVersion = Semver.coerce(matcher.group(3));
            return Optional.of(new DependencyRange(startingVersion, endingVersion));
        }

        pattern = Pattern.compile("Bump (.+) from (\\d+\\.\\d+(\\.\\d+)*).* to (\\d+\\.\\d+(\\.\\d+)*).*");
        matcher = pattern.matcher(pr.getTitle());
        if (matcher.find()) {
            var initialVersion = matcher.group(2).replaceAll("(\\d+.\\d+.\\d+).\\d+", "$1");
            var startingVersion = Semver.coerce(initialVersion);
            var targetVersion = matcher.group(3);
            if (!StringUtils.hasText(targetVersion) || targetVersion.startsWith(".")) {
                targetVersion = matcher.group(4);
            }
            targetVersion = targetVersion.replaceAll("(\\d+.\\d+.\\d+).\\d+", "$1");

            var endingVersion = Semver.coerce(targetVersion);
            return Optional.of(new DependencyRange(startingVersion, endingVersion));
        }


        pattern = Pattern.compile("chore\\(deps\\): bump (.+) from (\\d+\\.\\d+(\\.\\d+)*).* to (\\d+\\.\\d+(\\.\\d+)*).*");
        matcher = pattern.matcher(pr.getTitle());
        if (matcher.find()) {
            var initialVersion = matcher.group(2);
            var startingVersion = Semver.coerce(initialVersion.replaceAll("(\\d+.\\d+.\\d+).\\d+", "$1"));
            var targetVersion = matcher.group(3);
            if (!StringUtils.hasText(targetVersion) || targetVersion.startsWith(".")) {
                targetVersion = matcher.group(4);
            }

            var endingVersion = Semver.coerce(targetVersion.replaceAll("(\\d+.\\d+.\\d+).\\d+", "$1"));
            return Optional.of(new DependencyRange(startingVersion, endingVersion));
        }
        return Optional.empty();
    }

    private boolean canBotPullRequestBeMerged(final PullRequest pr) {
        if (pr.isBot() && !pr.isDraft() && !pr.isWorkInProgress() && !pr.isLocked()) {
            var files = getPullRequestFiles(pr);
            if (files.stream().allMatch(file -> file.getFilename().endsWith("locust/requirements.txt"))) {
                log.info("Merging bot pull request for Locust {}", pr);
                return approveAndMergePullRequest(pr, false);
            }
            if (files.stream().allMatch(file -> file.getFilename().matches(".github/workflows/.+.yml"))) {
                var rangeResult = extractBotDependencyRange(pr);
                if (rangeResult.isPresent() && (rangeResult.get().isQualifiedForPatchUpgrade() || rangeResult.get().isQualifiedForMinorUpgrade())) {
                    log.info("Merging bot pull request {} for GitHub Actions for dependency range {}", pr, rangeResult.get());
                    return approveAndMergePullRequest(pr, false);
                }
            }
        }
        return false;
    }

    public boolean autoMergePullRequest(final PullRequest pr) {
        if (pr.isMergeable() && pr.isBot()) {
            if (canBotPullRequestBeMerged(pr)) {
                return true;
            }
            try {
                var rangeResult = extractBotDependencyRange(pr);
                if (rangeResult.isPresent()) {
                    var dependencyVersion = rangeResult.get();
                    var stagingRepository = pr.isTargetedAtRepository(gitHubProperties.getStagingRepository().getFullName());

                    var startingVersion = dependencyVersion.startingVersion();
                    var endingVersion = dependencyVersion.endingVersion();

                    if (dependencyVersion.isQualifiedForPatchUpgrade()) {
                        log.info("Merging patch dependency upgrade {} from {} to {}", pr, startingVersion, endingVersion);
                        labelPullRequestAs(pr, CasLabels.LABEL_SKIP_CI);
                        return approveAndMergePullRequest(pr, false);
                    }

                    var files = getPullRequestFiles(pr);
                    if (files.size() == 1) {
                        var firstFile = files.getFirst().getFilename();

                        if (firstFile.endsWith("package.json")) {
                            if (dependencyVersion.isQualifiedForMinorUpgrade()) {
                                log.info("Merging minor dependency upgrade {} from {} to {}", pr, startingVersion, endingVersion);
                                labelPullRequestAs(pr, CasLabels.LABEL_SKIP_CI);
                                return approveAndMergePullRequest(pr, false);
                            }
                        }

                        if (firstFile.endsWith("libs.versions.toml") || firstFile.endsWith("settings.gradle")) {
                            if (stagingRepository && dependencyVersion.isQualifiedForMinorUpgrade()) {
                                if (pr.getAssignee() == null) {
                                    log.info("Assigning dependency upgrade {} from {} to {}", pr, startingVersion, endingVersion);
                                    gitHub.assignPullRequest(getOrganization(), getName(), pr, Users.APEREO_CAS_BOT);
                                } else if (pr.getAssignee().getLogin().equalsIgnoreCase(Users.APEREO_CAS_BOT)) {
                                    var checkrun = getLatestCompletedCheckRunsFor(pr, "build-pull-request");
                                    if (checkrun == null || checkrun.getCount() == 0) {
                                        log.info("Unassigning and re-assigning dependency upgrade {} from {} to {}", pr, startingVersion, endingVersion);
                                        gitHub.unassignPullRequest(getOrganization(), getName(), pr, Users.APEREO_CAS_BOT);
                                        gitHub.assignPullRequest(getOrganization(), getName(), pr, Users.APEREO_CAS_BOT);
                                    }
                                    if (checkrun != null && checkrun.getCount() == 1) {
                                        var run = checkrun.getRuns().getFirst();
                                        if (run.getStatus().equalsIgnoreCase(Workflows.WorkflowRunStatus.COMPLETED.getName())
                                            && run.getConclusion().equalsIgnoreCase("success")) {
                                            log.info("Merging dependency upgrade {} from {} to {}", pr, startingVersion, endingVersion);
                                            labelPullRequestAs(pr, CasLabels.LABEL_SKIP_CI);
                                            return approveAndMergePullRequest(pr, false);
                                        }
                                    }
                                }
                            }
                        }

                        if (firstFile.endsWith("Dockerfile") || firstFile.matches(".*ci\\/tests\\/.+\\/.+\\.sh")) {
                            if (dependencyVersion.isQualifiedForMinorUpgrade() || dependencyVersion.isQualifiedForPatchUpgrade()) {
                                log.info("Merging dependency upgrade {} from {} to {}", pr, startingVersion, endingVersion);
                                labelPullRequestAs(pr, CasLabels.LABEL_SKIP_CI);
                                return approveAndMergePullRequest(pr, false);
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if (pr.isMergeable() && pr.isLabeledAs(CasLabels.LABEL_AUTO_MERGE)) {
            var timeline = getPullRequestTimeline(pr);
            var admins = getGitHubProperties().getRepository().getAdmins();
            var approvedByAdmin = timeline
                    .stream()
                    .anyMatch(timelineEntry ->
                            timelineEntry.isLabeled()
                            && timelineEntry.getLabel().getName().equals(CasLabels.LABEL_AUTO_MERGE.getTitle())
                            && timelineEntry.getActor() != null
                            && admins.contains(timelineEntry.getActor().getLogin()));
            if (approvedByAdmin) {
                log.info("Merging admin-approved pull request {}", pr);
                return approveAndMergePullRequest(pr);
            }
        }
        return false;
    }

    private static Predicate<Label> getLabelPredicateByName(final CasLabels name) {
        return l -> l.getName().equals(name.getTitle());
    }

    public static Optional<Milestone> getMilestoneForBranch(final List<Milestone> milestones, final String branch) {
        val branchVersion = Version.parse(branch.replace(".x", "." + Integer.MAX_VALUE));
        return milestones.stream()
                .filter(milestone -> {
                    val milestoneVersion = Version.parse(milestone.getTitle());
                    return milestoneVersion.majorVersion() == branchVersion.majorVersion()
                           && milestoneVersion.minorVersion() == branchVersion.minorVersion();
                })
                .findFirst();
    }

    public static String getBranchForMilestone(final Milestone ms, final Optional<Milestone> master) {
        if (master.isPresent() && master.get().getNumber().equalsIgnoreCase(ms.getNumber())) {
            return "master";
        }
        val branchVersion = Version.parse(ms.getTitle());
        return branchVersion.majorVersion() + "." + branchVersion.minorVersion() + ".x";
    }

    public Version getCurrentVersionInMaster() {
        try {
            val rest = new RestTemplate();

            val url = String.format("https://raw.githubusercontent.com/%s/%s/master/gradle.properties",
                    repositoryProperties.getOrganization(),
                    repositoryProperties.getName());

            val uri = URI.create(url);
            val entity = rest.getForEntity(uri, String.class);
            val properties = new Properties();
            properties.load(new StringReader(Objects.requireNonNull(entity.getBody())));
            var version = properties.get("version").toString();
            log.info("Version found in CAS codebase is {}", version);
            currentVersionInMaster = Version.valueOf(version);
            log.info("Current master version is {}", currentVersionInMaster);
            return currentVersionInMaster;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        throw new RuntimeException("Unable to determine version in master branch");
    }

    public List<Branch> getActiveBranches() {
        var branches = new ArrayList<Branch>();
        try {
            var br = gitHub.getBranches(getOrganization(), getName());
            while (br != null) {
                branches.addAll(br.getContent());
                br = br.next();
            }
            log.info("Available branches are {}", branches.stream().map(Object::toString).collect(Collectors.joining(",")));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        var milestones = getActiveMilestones();
        return branches
                .stream()
                .filter(branch -> {
                    if (branch.isMasterBranch()) {
                        return true;
                    }
                    if (branch.isMilestoneBranch() && getMilestoneForBranch(milestones, branch.getName()).isEmpty()) {
                        return false;
                    }
                    if (branch.isGhPagesBranch() || branch.isHerokuBranch()) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public String getFullName() {
        return repositoryProperties.getFullName();
    }

    public List<Milestone> getActiveMilestones() {
        final List<Milestone> milestones = new ArrayList<>();
        try {
            var page = gitHub.getMilestones(getOrganization(), getName());
            while (page != null) {
                milestones.addAll(page.getContent());
                page = page.next();
            }
            log.info("Available milestones are {}", milestones);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return milestones;
    }

    public String getOrganization() {
        return repositoryProperties.getOrganization();
    }

    public String getName() {
        return repositoryProperties.getName();
    }

    public Optional<Milestone> getMilestoneForMaster() {
        var milestones = getActiveMilestones();

        var currentVersion = Version.valueOf(currentVersionInMaster.toString().replace("-SNAPSHOT", ""));
        return milestones
                .stream()
                .sorted()
                .filter(milestone -> {
                    var masterVersion = Version.valueOf(milestone.getTitle());
                    return masterVersion.majorVersion() == currentVersion.majorVersion()
                           && masterVersion.minorVersion() == currentVersion.minorVersion();
                })
                .findFirst();
    }

    public PullRequest mergePullRequestWithBase(final PullRequest pr) {
        return this.gitHub.mergeWithBase(getOrganization(), getName(), pr);
    }

    public boolean mergePullRequestIntoBase(final PullRequest pr) {
        var commitTitle = pr.getTitle();
        var commitMessage = pr.getBody();
        if (pr.isLabeledAs(CasLabels.LABEL_SKIP_CI)) {
            commitTitle += " [ci skip]";
            commitMessage += " [ci skip]";
        }
        log.debug("Merging pull request {} into base branch", pr);
        return gitHub.mergeIntoBase(getOrganization(), getName(),
                pr, commitTitle, commitMessage,
                pr.getHead().getSha(), "squash");
    }

    public List<PullRequestReview> getPullRequestReviews(final PullRequest pr) {
        List<PullRequestReview> files = new ArrayList<>();
        var pages = this.gitHub.getPullRequestReviews(getOrganization(), getName(), pr);
        while (pages != null) {
            files.addAll(pages.getContent());
            pages = pages.next();
        }
        return files;
    }

    public List<TimelineEntry> getPullRequestTimeline(final PullRequest pr) {
        var files = new ArrayList<TimelineEntry>();
        var pages = this.gitHub.getPullRequestTimeline(getOrganization(), getName(), pr);
        while (pages != null) {
            files.addAll(pages.getContent());
            pages = pages.next();
        }
        return files;
    }

    public PullRequest labelPullRequestAs(final PullRequest pr, final CasLabels... labelName) {
        var labels = Arrays.stream(labelName).filter(label -> !pr.isLabeledAs(label)).map(CasLabels::getTitle).toList();
        return gitHub.addLabel(pr, labels);
    }

    public void removeAllCommentsFrom(final PullRequest pr, final String login) {
        val comments = getAllCommentsFor(pr).stream().filter(comment -> comment.getUser().getLogin().equalsIgnoreCase(login)).toList();
        comments.forEach(comment -> {
            gitHub.removeComment(getOrganization(), getName(), comment.getId());
        });
    }

    public void removeAllApereoCasBotCommentsFrom(final PullRequest pr) {
        removeAllCommentsFrom(pr, Users.APEREO_CAS_BOT);
    }

    private List<Comment> getAllCommentsFor(final PullRequest pr) {
        var allComments = new ArrayList<Comment>();
        var pages = this.gitHub.getComments(pr);
        while (pages != null) {
            allComments.addAll(pages.getContent());
            pages = pages.next();
        }
        allComments.sort(Collections.reverseOrder(Comparator.comparing(Comment::getUpdatedTime)));
        return allComments;
    }

    public void addComment(final PullRequest pr, final String comment) {
        this.gitHub.addComment(pr, comment);
    }

    public void removeLabelFrom(final PullRequest pr, final CasLabels labelName) {
        this.gitHub.removeLabel(pr, labelName.getTitle());
    }

    public void close(final PullRequest pr) {
        this.gitHub.closePullRequest(this.getOrganization(), getName(), pr.getNumber());
    }

    public void open(final PullRequest pr) {
        this.gitHub.openPullRequest(this.getOrganization(), getName(), pr.getNumber());
    }

    public List<PullRequestFile> getPullRequestFiles(final PullRequest pr) {
        return getPullRequestFiles(pr.getNumber());
    }

    public boolean approveAndMergePullRequest(final PullRequest pr) {
        return approveAndMergePullRequest(pr, true);
    }

    public boolean approveAndMergePullRequest(final PullRequest pr, final boolean includeComment) {
        if (pr.isApereoCasBot() || approvePullRequest(pr, includeComment)) {
            return mergePullRequestIntoBase(pr);
        }
        return false;
    }

    public List<PullRequestFile> getPullRequestFiles(final String pr) {
        List<PullRequestFile> files = new ArrayList<>();
        var pages = this.gitHub.getPullRequestFiles(getOrganization(), getName(), pr);
        while (pages != null) {
            files.addAll(pages.getContent());
            pages = pages.next();
        }
        return files;
    }

    public PullRequest updatePullRequestTitle(PullRequest pullRequest, String title) {
        gitHub.updatePullRequest(this.getOrganization(), getName(), pullRequest, Map.of("title", title));
        return getPullRequest(pullRequest.getNumber());
    }

    public PullRequest getPullRequest(String number) {
        return this.gitHub.getPullRequest(getOrganization(), getName(), number);
    }

    public List<Commit> getPullRequestCommits(final PullRequest pr) {
        List<Commit> commits = new ArrayList<>();
        var pages = this.gitHub.getPullRequestCommits(getOrganization(), getName(), pr.getNumber());
        while (pages != null) {
            commits.addAll(pages.getContent());
            pages = pages.next();
        }
        commits.sort(Collections.reverseOrder(Comparator.comparing(c -> c.getCommit().getAuthor().getDate())));
        return commits;
    }

    public Commit getHeadCommitFor(final Branch shaOrBranch) {
        return this.gitHub.getCommit(getOrganization(), getName(), shaOrBranch.getName());
    }

    public Commit getHeadCommitFor(final String shaOrBranch) {
        return this.gitHub.getCommit(getOrganization(), getName(), shaOrBranch);
    }

    public Commit getCommit(final String commitSha) {
        return this.gitHub.getCommit(getOrganization(), getName(), commitSha);
    }

    public CheckRun getLatestCompletedCheckRunsFor(final PullRequest pr, String checkName) {
        return this.gitHub.getCheckRunsFor(getOrganization(), getName(),
                pr.getHead().getSha(), checkName, "completed", "latest");
    }

    public CheckRun getLatestCompletedCheckRun(final PullRequest pr) {
        return getLatestCompletedCheckRunsFor(pr, null);
    }

    public boolean createCheckRunForActionRequired(final PullRequest pr, String checkName,
                                                   final String title, final String summary) {
        try {
            val output = Map.of("title", title, "summary", summary);
            return this.gitHub.createCheckRun(getOrganization(), getName(),
                    checkName, pr.getHead().getSha(), "completed", "action_required", output);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean createStatusForSuccess(final PullRequest pr, final String context, String description) {
        try {
            return this.gitHub.createStatus(getOrganization(), getName(),
                    pr.getHead().getSha(), "success", "https://apereo.github.io/cas",
                    description, context);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean createStatusForFailure(final PullRequest pr, final String context, String description) {
        try {
            return this.gitHub.createStatus(getOrganization(), getName(),
                    pr.getHead().getSha(), "failure", "https://apereo.github.io/cas",
                    description, context);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public List<CommitStatus> getPullRequestCommitStatuses(final PullRequest pr) {
        val results = new ArrayList<CommitStatus>();
        var pages = this.gitHub.getPullRequestCommitStatus(pr);
        while (pages != null) {
            results.addAll(pages.getContent());
            pages = pages.next();
        }
        return results;
    }

    public CombinedCommitStatus getCombinedPullRequestCommitStatuses(final PullRequest pr) {
        return this.gitHub.getCombinedPullRequestCommitStatus(getOrganization(), getName(), pr.getHead().getSha());
    }

    public void cancelQualifyingWorkflowRuns(final List<Branch> currentBranches) {
        currentBranches.forEach(branch -> {
            var workflowRun = gitHub.getWorkflowRuns(getOrganization(), getName(),
                    branch, Workflows.WorkflowRunStatus.QUEUED);
            cancelQualifyingWorkflowRuns(workflowRun);
        });

        var workflowRun = gitHub.getWorkflowRuns(getOrganization(), getName(),
                Workflows.WorkflowRunEvent.PULL_REQUEST, Workflows.WorkflowRunStatus.QUEUED);
        cancelQualifyingWorkflowRuns(workflowRun);
        cancelWorkflowRunsForMissingPullRequests(workflowRun);
    }

    public void removeCancelledWorkflowRuns() {
        var workflowRun = gitHub.getWorkflowRuns(getOrganization(), getName(), Workflows.WorkflowRunStatus.CANCELLED);
        if (!workflowRun.getRuns().isEmpty()) {
            log.debug("Found {} cancelled workflow runs", workflowRun.getRuns().size());
            workflowRun.getRuns().forEach(run -> {
                log.debug("Removing workflow run {}", run);
                gitHub.removeWorkflowRun(getOrganization(), getName(), run);
            });
        }

        workflowRun = gitHub.getWorkflowRuns(getOrganization(), getName(), Workflows.WorkflowRunStatus.SKIPPED);
        if (!workflowRun.getRuns().isEmpty()) {
            log.debug("Found {} cancelled workflow runs", workflowRun.getRuns().size());
            workflowRun.getRuns().forEach(run -> {
                log.debug("Removing workflow run {}", run);
                gitHub.removeWorkflowRun(getOrganization(), getName(), run);
            });
        }
    }

    public boolean shouldResumeCiBuild(final PullRequest pr) {
        var allComments = getAllCommentsFor(pr);
        if (!allComments.isEmpty()) {
            for (final Comment lastComment : allComments) {
                var body = lastComment.getBody().trim();
                val runci = body.equals("@apereocas-bot runci");
                if (repositoryProperties.getCommitters().contains(lastComment.getUser().getLogin()) && runci) {
                    gitHub.removeComment(getOrganization(), getName(), lastComment.getId());
                    return true;
                }
            }
        }
        return false;
    }

    public void removePullRequestWorkflowRunsForMissingBranches() {
        var workflowRun = gitHub.getWorkflowRuns(getOrganization(), getName(), Workflows.WorkflowRunEvent.PULL_REQUEST);
        if (!workflowRun.getRuns().isEmpty()) {
            log.info("Found {} workflow runs for pull requests", workflowRun.getCount());
        }
        var pullRequests = new ArrayList<PullRequest>();
        var pages = this.gitHub.getPullRequests(getOrganization(), getName());
        while (pages != null) {
            pullRequests.addAll(pages.getContent());
            pages = pages.next();
        }
        workflowRun.getRuns().forEach(run -> {
            val found = pullRequests.stream().filter(pr -> pr.getHead().getRef().equals(run.getHeadBranch())).findFirst();
            if (found.isEmpty()) {
                Workflows.WorkflowRunStatus.from(run)
                        .filter(status -> status == Workflows.WorkflowRunStatus.IN_PROGRESS)
                        .ifPresent(status -> cancelWorkflowRun(run));

                log.debug("Removing workflow run {} without an active pull request", run);
                gitHub.removeWorkflowRun(getOrganization(), getName(), run);
            } else {
                var pr = found.get();
                var foundci = pr.getLabels().stream().anyMatch(label -> label.getName().equalsIgnoreCase(CasLabels.LABEL_CI.getTitle()));
                if (!repositoryProperties.getCommitters().contains(pr.getUser().getLogin()) && !foundci) {
                    log.debug("Removing workflow run {} without the label {}", run, CasLabels.LABEL_CI.getTitle());
                    gitHub.removeWorkflowRun(getOrganization(), getName(), run);
                }
            }
        });
    }

    public Set<Workflows.WorkflowRun> rerunFailedWorkflowJobs() {
        var commit = getHeadCommitFor("master");
        var workflowRuns = gitHub.getWorkflowRuns(getOrganization(), getName(), commit, Workflows.WorkflowRunStatus.FAILURE);
        return workflowRuns
                .getRuns()
                .stream()
                .filter(run -> WorkflowRuns.isAnyOf(run.getName()) && run.getRunAttempt() == 1)
                .peek(run -> {
                    log.info("Rerunning failed workflow run {}", run);
                    gitHub.rerunFailedWorkflowJobs(getOrganization(), getName(), run);
                })
                .collect(Collectors.toSet());
    }

    public void removeOldWorkflowRuns() {
        val now = OffsetDateTime.now();
        for (var i = 10; i > 0; i--) {
            var workflowRun = gitHub.getWorkflowRuns(getOrganization(), getName(), i);
            if (!workflowRun.getRuns().isEmpty()) {
                log.debug("Found {} workflow runs for page {}", workflowRun.getRuns().size(), i);
            }

            workflowRun.getRuns().forEach(run -> {
                val staleExp = run.getUpdatedTime().plusDays(gitHubProperties.getStaleWorkflowRunInDays());
                if (staleExp.isBefore(now)) {
                    Workflows.WorkflowRunStatus.from(run)
                            .filter(status -> status == Workflows.WorkflowRunStatus.IN_PROGRESS)
                            .ifPresent(status -> cancelWorkflowRun(run));

                    log.info("Removing old workflow run {} @ {}", run, run.getUpdatedTime());
                    gitHub.removeWorkflowRun(getOrganization(), getName(), run);
                } else if (run.isRemovable()) {
                    if (run.getName().equalsIgnoreCase(WorkflowRuns.DEPENDENCY_SUBMISSION_MAVEN.getName())) {
                        log.info("Removing Maven dependency submission workflow run {} @ {}", run, run.getUpdatedTime());
                        gitHub.removeWorkflowRun(getOrganization(), getName(), run);
                    } else if (run.getName().equalsIgnoreCase(WorkflowRuns.DEPENDENCY_SUBMISSION_GRADLE.getName())) {
                        log.info("Removing Gradle dependency submission workflow run {} @ {}", run, run.getUpdatedTime());
                        gitHub.removeWorkflowRun(getOrganization(), getName(), run);
                    } else if (run.isConcludedSuccessfully() || run.isSkipped()) {
                        val completedExp = run.getUpdatedTime().plusDays(gitHubProperties.getCompletedSuccessfulWorkflowRunInDays());
                        if (completedExp.isBefore(now)) {
                            log.info("Removing completed successful workflow run {} @ {}", run, run.getUpdatedTime());
                            gitHub.removeWorkflowRun(getOrganization(), getName(), run);
                        }
                    } else {
                        val completedExp = run.getUpdatedTime().plusDays(gitHubProperties.getCompletedFailedWorkflowRunInDays());
                        if (completedExp.isBefore(now)) {
                            log.info("Removing completed failed workflow run {} @ {}", run, run.getUpdatedTime());
                            gitHub.removeWorkflowRun(getOrganization(), getName(), run);
                        }
                    }
                } else if (run.getName().equalsIgnoreCase(WorkflowRuns.RERUN_WORKFLOWS.getName())
                           && (run.isConcludedSuccessfully() || run.isSkipped())) {
                    log.info("Removing rerun workflow {} @ {}", run, run.getUpdatedTime());
                    gitHub.removeWorkflowRun(getOrganization(), getName(), run);
                }
            });
        }
    }


    private void cancelWorkflowRunsForMissingPullRequests(final Workflows workflows) {
        var runs = new ArrayList<>(workflows.getRuns());

        var pullRequests = new ArrayList<PullRequest>();
        var pages = this.gitHub.getPullRequests(getOrganization(), getName());
        while (pages != null) {
            pullRequests.addAll(pages.getContent());
            pages = pages.next();
        }

        runs.forEach(run -> {
            val found = pullRequests.stream().anyMatch(pr -> pr.getHead().getRef().equals(run.getHeadBranch()));
            if (!found) {
                log.debug("Cancelling workflow run {}. No open pull request found for branch {}", run, run.getHeadBranch());
                cancelWorkflowRun(run);
            }
        });
    }

    private List<Label> getActiveLabels() {
        final List<Label> labels = new ArrayList<>();
        try {
            var lbl = gitHub.getLabels(getOrganization(), getName());
            while (lbl != null) {
                labels.addAll(lbl.getContent());
                lbl = lbl.next();
            }
            log.info("Available labels are {}", labels.stream().map(Object::toString).collect(Collectors.joining(",")));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return labels;
    }

    private void cancelQualifyingWorkflowRuns(final Workflows workflows) {
        var runs = new ArrayList<>(workflows.getRuns());
        runs.sort(Collections.reverseOrder(Comparator.comparingLong(Workflows.WorkflowRun::getRunNumber)));

        var groupedRuns = new HashMap<String, Workflows.WorkflowRun>(runs.size());
        var runsToCancel = new HashSet<Workflows.WorkflowRun>();

        runs.forEach(run -> {
            var key = run.getHeadBranch() + '@' + run.getName();
            if (!groupedRuns.containsKey(key)) {
                groupedRuns.put(key, run);
            } else {
                runsToCancel.add(run);
            }
        });
        runsToCancel.forEach(this::cancelWorkflowRun);
    }

    private void cancelWorkflowRun(final Workflows.WorkflowRun run) {
        try {
            log.debug("Cancelling workflow run {}", run);
            this.gitHub.cancelWorkflowRun(getOrganization(), getName(), run);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }


}
