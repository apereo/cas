package org.apereo.cas.prs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.CasLabels;
import org.apereo.cas.MonitoredRepository;
import org.apereo.cas.PullRequestListener;
import org.apereo.cas.github.CombinedCommitStatus;
import org.apereo.cas.github.PullRequest;
import org.apereo.cas.github.PullRequestFile;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class CasPullRequestListener implements PullRequestListener {
    private final MonitoredRepository repository;

    @Override
    public void onOpenPullRequest(final PullRequest givenPullRequest) {
        val pr = this.repository.getPullRequest(givenPullRequest.getNumber());
        log.debug("Processing {}", pr);

        if (processLabelSeeMaintenancePolicy(pr) || processInvalidPullRequest(pr)) {
            return;
        }
        processLabelPendingPortForward(pr);
        processLabelPendingUpdateProperty(pr);
        processMilestoneAssignment(pr);
        processLabelsByFeatures(pr);
        removeLabelWorkInProgress(pr);
        checkForPullRequestTestCases(pr);
        mergePullRequestIfPossible(pr);
    }

    private void checkForPullRequestTestCases(final PullRequest pr) {
        val files = repository.getPullRequestFiles(pr);
        val modifiesJava = files.stream().anyMatch(file -> !file.getFilename().contains("Tests") && file.getFilename().endsWith(".java"));
        if (modifiesJava) {
            val hasTests = files.stream().anyMatch(file -> file.getFilename().endsWith("Tests.java"));
            if (!hasTests) {
                log.info("Pull request {} does not have any tests", pr);
                if (!pr.isLabeledAs(CasLabels.LABEL_PENDING_NEEDS_TESTS)) {
                    repository.labelPullRequestAs(pr, CasLabels.LABEL_PENDING_NEEDS_TESTS);
                }
                repository.createStatusForFailure(pr, "Tests", "Please add tests to verify changes.");
            } else {
                if (pr.isLabeledAs(CasLabels.LABEL_PENDING_NEEDS_TESTS)) {
                    repository.removeLabelFrom(pr, CasLabels.LABEL_PENDING_NEEDS_TESTS);
                }
                repository.createStatusForSuccess(pr, "Tests", "Good job! A positive pull request.");
            }
        }
    }

    private void mergePullRequestIfPossible(final PullRequest pr) {
        if (pr.isLabeledAs(CasLabels.LABEL_BOT) && pr.isLabeledAs(CasLabels.LABEL_DEPENDENCIES_MODULES)) {
            val checkRun = this.repository.getCombinedPullRequestCommitStatuses(pr);
            if (checkRun.isCheckStatusSuccess(CombinedCommitStatus.TRAVIS_CI)) {
                this.repository.mergePullRequestIntoBase(pr);
            }
        }
    }

    private boolean processInvalidPullRequest(final PullRequest pr) {
        if (pr.getChangedFiles() >= 40) {
            log.info("Closing invalid pull request {} with large number of changes", pr);
            repository.labelPullRequestAs(pr, CasLabels.LABEL_PROPOSAL_DECLINED);
            repository.labelPullRequestAs(pr, CasLabels.LABEL_SEE_CONTRIBUTOR_GUIDELINES);
            repository.addComment(pr, "Thank you very much for submitting this pull request! \n\nThis patch contains "
                + "a very large number of commits or changed files and is quite impractical to evaluate and review. "
                + "Please make sure your changes are broken down into smaller pull requests so that members can assist and review "
                + "as quickly as possible. Furthermore, make sure your patch is based on the appropriate branch, is a feature branch and "
                + "targets the correct CAS branch here to avoid conflicts. \n"
                + "If you believe this to be an error, please post your explanation here as a comment and it will be reviewed as quickly as possible. \n"
                + "For additional details, please review https://apereo.github.io/cas/developer/Contributor-Guidelines.html"
                + "\n\nIf you are seeking assistance and have a question about your CAS deployment, "
                + "please visit https://apereo.github.io/cas/Support.html to learn more about support options.");
            repository.close(pr);
            return true;
        }
        return false;
    }

    private void removeLabelWorkInProgress(final PullRequest pr) {
        if (pr.isDraft()) {
            if (pr.isLabeledAs(CasLabels.LABEL_PENDING)) {
                repository.removeLabelFrom(pr, CasLabels.LABEL_PENDING);
            }
            if (!pr.isLabeledAs(CasLabels.LABEL_WIP)) {
                repository.labelPullRequestAs(pr, CasLabels.LABEL_WIP);
            }
        } else if (pr.isLabeledAs(CasLabels.LABEL_WIP)) {
            if (pr.isLabeledAs(CasLabels.LABEL_PENDING)) {
                repository.removeLabelFrom(pr, CasLabels.LABEL_PENDING);
            }
            val title = pr.getTitle().toLowerCase();
            if (CasLabels.LABEL_WIP.getKeywords() != null && !CasLabels.LABEL_WIP.getKeywords().matcher(title).find()) {
                log.info("{} will remove the label {}", pr, CasLabels.LABEL_WIP);
                repository.removeLabelFrom(pr, CasLabels.LABEL_WIP);
            }
        }
    }

    private void processLabelPendingUpdateProperty(final PullRequest pr) {
        if (!pr.isLabeledAs(CasLabels.LABEL_PENDING_DOCUMENT_PROPERTY)) {
            Collection<PullRequestFile> files = repository.getPullRequestFiles(pr);
            val hasProperty = files.stream().anyMatch(f -> f.getFilename().endsWith("Properties.java"));
            if (hasProperty) {
                val hasNoDocs = files.stream().noneMatch(f -> f.getFilename().contains("Configuration-Properties.md"));
                if (hasNoDocs) {
                    log.info("{} changes CAS properties, yet documentation is not updated to reflect changes", pr);
                    if (!pr.isLabeledAs(CasLabels.LABEL_PENDING_DOCUMENT_PROPERTY)) {
                        repository.labelPullRequestAs(pr, CasLabels.LABEL_PENDING_DOCUMENT_PROPERTY);
                    }
                } else {
                    if (pr.isLabeledAs(CasLabels.LABEL_PENDING_DOCUMENT_PROPERTY)) {
                        repository.removeLabelFrom(pr, CasLabels.LABEL_PENDING_DOCUMENT_PROPERTY);
                    }
                }
            }
        }
    }

    private boolean processLabelSeeMaintenancePolicy(final PullRequest pr) {
        if (!pr.isTargetedAtMasterBranch() && !pr.isLabeledAs(CasLabels.LABEL_SEE_MAINTENANCE_POLICY)) {
            val milestone = repository.getMilestoneForBranch(pr.getBase().getRef());
            if (milestone.isEmpty()) {
                log.info("{} is targeted at a branch {} that is no longer maintained. See maintenance policy", pr, pr.getBase());
                repository.labelPullRequestAs(pr, CasLabels.LABEL_SEE_MAINTENANCE_POLICY);
                repository.labelPullRequestAs(pr, CasLabels.LABEL_PROPOSAL_DECLINED);
                repository.addComment(pr, "Thank you very much for submitting this pull request! Please note that this patch "
                    + "is targeted at a CAS branch that is no longer maintained and as such cannot be accepted or merged. "
                    + "For additional details, please review https://apereo.github.io/cas/developer/Maintenance-Policy.html"
                    + "\n\nIf you are seeking assistance or have a question about your CAS deployment, "
                    + "please visit https://apereo.github.io/cas/Support.html for support options.");
                repository.close(pr);
                return true;
            }
        }
        return false;
    }

    private void processLabelPendingPortForward(final PullRequest pr) {
        if (!pr.getBase().isRefMaster() && !pr.isLabeledAs(CasLabels.LABEL_PENDING_PORT_FORWARD)) {
            log.info("{} is targeted at a branch {} and should be ported forward to the master branch in a separate pull request.", pr, pr.getBase());
            repository.labelPullRequestAs(pr, CasLabels.LABEL_PENDING_PORT_FORWARD);
        }
    }

    private void processMilestoneAssignment(final PullRequest pr) {
        if (pr.getMilestone() == null) {
            if (pr.isTargetedAtMasterBranch()) {
                val milestoneForMaster = repository.getMilestoneForMaster();
                milestoneForMaster.ifPresent(milestone -> {
                    log.info("{} will be assigned the master milestone {}", pr, milestone);
                    repository.getGitHub().setMilestone(pr, milestone);
                });
            } else {
                val milestone = repository.getMilestoneForBranch(pr.getBase().getRef());
                milestone.ifPresent(result -> {
                    log.info("{} will be assigned the maintenance milestone {}", pr, milestone);
                    repository.getGitHub().setMilestone(pr, result);
                });
            }
        }
    }

    private void processLabelsByFeatures(final PullRequest givenPullRequest) {
        val pr = this.repository.getPullRequest(givenPullRequest.getNumber());
        val title = pr.getTitle().toLowerCase();
        Arrays.stream(CasLabels.values()).forEach(l -> {
            if (!pr.isLabeledAs(l)) {
                val titlePattern = Pattern.compile("\\b" + l.getTitle().toLowerCase() + ":*\\b", Pattern.CASE_INSENSITIVE);
                if (titlePattern.matcher(pr.getTitle()).find()) {
                    log.info("{} will be assigned the label {}", pr, l);
                    repository.labelPullRequestAs(pr, l);
                } else if (l.getKeywords() != null && l.getKeywords().matcher(title).find()) {
                    log.info("{} will be assigned the label {} by keywords", pr, l);
                    repository.labelPullRequestAs(pr, l);
                }
            }
        });
    }
}
