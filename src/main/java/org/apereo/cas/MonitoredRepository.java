package org.apereo.cas;

import lombok.val;
import org.apereo.cas.github.CheckRun;
import org.apereo.cas.github.CombinedCommitStatus;
import org.apereo.cas.github.Commit;
import org.apereo.cas.github.CommitStatus;
import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.Label;
import org.apereo.cas.github.Milestone;
import org.apereo.cas.github.Page;
import org.apereo.cas.github.PullRequest;
import org.apereo.cas.github.PullRequestFile;

import com.github.zafarkhaja.semver.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A repository that should be monitored.
 *
 * @author Misagh Moayyed
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class MonitoredRepository {
    private final GitHubOperations gitHub;

    private final GitHubProperties gitHubProperties;

    private Version currentVersionInMaster;

    private static Predicate<Label> getLabelPredicateByName(final CasLabels name) {
        return l -> l.getName().equals(name.getTitle());
    }

    public Version getCurrentVersionInMaster() {
        try {
            val rest = new RestTemplate();
            val uri = URI.create(gitHubProperties.getRepository().getUrl() + "/raw/master/gradle.properties");
            val entity = rest.getForEntity(uri, String.class);
            val properties = new Properties();
            properties.load(new StringReader(Objects.requireNonNull(entity.getBody())));
            final String version = properties.get("version").toString();
            log.info("Version found in CAS codebase is {}", version);
            currentVersionInMaster = Version.valueOf(version);
            log.info("Current master version is {}", currentVersionInMaster);
            return currentVersionInMaster;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        throw new RuntimeException("Unable to determine version in master branch");
    }

    private List<Label> getActiveLabels() {
        final List<Label> labels = new ArrayList<>();
        try {
            Page<Label> lbl = gitHub.getLabels(getOrganization(), getName());
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

    private List<Milestone> getActiveMilestones() {
        final List<Milestone> milestones = new ArrayList<>();
        try {
            Page<Milestone> page = gitHub.getMilestones(getOrganization(), getName());
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
        return this.gitHubProperties.getRepository().getOrganization();
    }

    public String getName() {
        return this.gitHubProperties.getRepository().getName();
    }

    public Optional<Milestone> getMilestoneForMaster() {
        List<Milestone> milestones = getActiveMilestones();

        final Version currentVersion = Version.valueOf(currentVersionInMaster.toString().replace("-SNAPSHOT", ""));
        Optional<Milestone> result = milestones
            .stream()
            .sorted()
            .filter(milestone -> {
                final Version masterVersion = Version.valueOf(milestone.getTitle());
                return masterVersion.getMajorVersion() == currentVersion.getMajorVersion()
                    && masterVersion.getMinorVersion() == currentVersion.getMinorVersion();
            })
            .findFirst();
        return result;
    }

    public Optional<Milestone> getMilestoneForBranch(final String branch) {
        val milestones = getActiveMilestones();
        val branchVersion = Version.valueOf(branch.replace(".x", "." + Integer.MAX_VALUE));
        return milestones.stream()
            .filter(milestone -> {
                val milestoneVersion = Version.valueOf(milestone.getTitle());
                return milestoneVersion.getMajorVersion() == branchVersion.getMajorVersion()
                    && milestoneVersion.getMinorVersion() == branchVersion.getMinorVersion();
            })
            .findFirst();
    }

    public PullRequest mergePullRequestWithBase(final PullRequest pr) {
        return this.gitHub.mergeWithBase(getOrganization(), getName(), pr);
    }

    public boolean mergePullRequestIntoBase(final PullRequest pr) {
        return this.gitHub.mergeIntoBase(getOrganization(), getName(), pr, pr.getTitle(), pr.getBody(),
            pr.getHead().getSha(), "squash");
    }

    public void labelPullRequestAs(final PullRequest pr, final CasLabels labelName) {
        this.gitHub.addLabel(pr, labelName.getTitle());
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

    public List<PullRequestFile> getPullRequestFiles(final PullRequest pr) {
        List<PullRequestFile> files = new ArrayList<>();
        Page<PullRequestFile> pages = this.gitHub.getPullRequestFiles(getOrganization(), getName(), pr.getNumber());
        while (pages != null) {
            files.addAll(pages.getContent());
            pages = pages.next();
        }
        return files;
    }

    public PullRequest getPullRequest(String number) {
        return this.gitHub.getPullRequest(getOrganization(), getName(), number);
    }

    public List<Commit> getPullRequestCommits(final PullRequest pr) {
        List<Commit> commits = new ArrayList<>();
        Page<Commit> pages = this.gitHub.getPullRequestCommits(getOrganization(), getName(), pr.getNumber());
        while (pages != null) {
            commits.addAll(pages.getContent());
            pages = pages.next();
        }
        return commits;
    }

    public CheckRun getLatestCompletedCheckRunsFor(final PullRequest pr, String checkName) {
        return this.gitHub.getCheckRunsFor(getOrganization(), getName(),
            pr.getHead().getSha(), checkName, "completed", "latest");
    }

    public CheckRun getLatestCompletedCheckRun(final PullRequest pr) {
        return getLatestCompletedCheckRunsFor(pr, null);
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
}
