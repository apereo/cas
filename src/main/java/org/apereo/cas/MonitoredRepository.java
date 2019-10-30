package org.apereo.cas;

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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * A repository that should be monitored.
 *
 * @author Andy Wilkinson
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class MonitoredRepository implements InitializingBean {
    private final GitHubOperations gitHub;
    private final GitHubProperties gitHubProperties;

    private final List<Milestone> milestones = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();

    private Version currentVersionInMaster;

    private static Predicate<Label> getLabelPredicateByName(final CasLabels name) {
        return l -> l.getName().equals(name.getTitle());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final RestTemplate rest = new RestTemplate();
        final URI uri = URI.create(gitHubProperties.getRepository().getUrl() + "/raw/master/gradle.properties");
        final ResponseEntity entity = rest.getForEntity(uri, String.class);
        final Properties properties = new Properties();
        properties.load(new StringReader(entity.getBody().toString()));
        currentVersionInMaster = Version.valueOf(properties.get("version").toString());

        log.info("Current master version is {}", currentVersionInMaster);

        Page<Milestone> page = gitHub.getMilestones(getOrganization(), getName());
        while (page != null) {
            milestones.addAll(page.getContent());
            page = page.next();
        }
        log.info("Available milestones are {}", this.milestones);

        Page<Label> lbl = gitHub.getLabels(getOrganization(), getName());
        while (lbl != null) {
            labels.addAll(lbl.getContent());
            lbl = lbl.next();
        }
        log.info("Available labels are {}", this.labels);
    }

    public String getOrganization() {
        return this.gitHubProperties.getRepository().getOrganization();
    }

    public String getName() {
        return this.gitHubProperties.getRepository().getName();
    }

    public Optional<Milestone> getMilestoneForMaster() {
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
        final Version branchVersion = Version.valueOf(branch.replace(".x", "." + Integer.MAX_VALUE));
        return milestones.stream()
            .filter(milestone -> {
                final Version milestoneVersion = Version.valueOf(milestone.getTitle());
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
        final List<CommitStatus> results = new ArrayList<>();
        Page<CommitStatus> pages = this.gitHub.getPullRequestCommitStatus(pr);
        while (pages != null) {
            results.addAll(pages.getContent());
            pages = pages.next();
        }
        return results;
    }

    public CombinedCommitStatus getCombinedPullRequestCommitStatuses(final PullRequest pr) {
        return this.gitHub.getCombinedPullRequestCommitStatus(getOrganization(), getName(), pr.getHead().getSha());
    }

    public String getCurrentCommitSha() {
        return null;
    }
}
