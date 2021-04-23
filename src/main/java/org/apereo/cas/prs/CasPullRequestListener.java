package org.apereo.cas.prs;

import org.apereo.cas.CasLabels;
import org.apereo.cas.MonitoredRepository;
import org.apereo.cas.PullRequestListener;
import org.apereo.cas.github.PullRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
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
        processLabelReadyForContinuousIntegration(pr);
        processLabelPendingPortForward(pr);
        processMilestoneAssignment(pr);
        processLabelsByFeatures(pr);
        processLabelsByChangeset(pr);
        removeLabelWorkInProgress(pr);
        checkForPullRequestTestCases(pr);
    }

    private void processLabelsByChangeset(final PullRequest pr) {
        repository.getPullRequestFiles(pr)
            .forEach(file -> {
                var fname = file.getFilename();
                if (fname.contains("api/cas-server-core-api-configuration-model") && !pr.isLabeledAs(CasLabels.LABEL_CONFIGURATION)) {
                    repository.labelPullRequestAs(pr, CasLabels.LABEL_CONFIGURATION);
                }
                if (fname.contains("dependencies.gradle") && !pr.isLabeledAs(CasLabels.LABEL_DEPENDENCIES_MODULES)) {
                    repository.labelPullRequestAs(pr, CasLabels.LABEL_DEPENDENCIES_MODULES);
                } else if (fname.endsWith(".gradle") && !pr.isLabeledAs(CasLabels.LABEL_GRADLE_BUILD_RELEASE)) {
                    repository.labelPullRequestAs(pr, CasLabels.LABEL_GRADLE_BUILD_RELEASE);
                }
            });
    }

    private void processLabelReadyForContinuousIntegration(final PullRequest pr) {
        val ci = repository.getGitHubProperties().getRepository().getCommitters().contains(pr.getUser().getLogin());
        if (ci && !pr.isLabeledAs(CasLabels.LABEL_CI)) {
            log.info("Pull request {} is for continuous integration", pr);
            repository.labelPullRequestAs(pr, CasLabels.LABEL_CI);
        }
        if (repository.shouldResumeCiBuild(pr)) {
            log.info("Pull request {} should resume CI workflow", pr);
            if (pr.isLabeledAs(CasLabels.LABEL_CI)) {
                repository.removeLabelFrom(pr, CasLabels.LABEL_CI);
            }
            repository.labelPullRequestAs(pr, CasLabels.LABEL_CI);
        }
    }

    private void checkForPullRequestTestCases(final PullRequest pr) {
        if (pr.isTargetBranchOnHeroku()) {
            log.info("Pull request {} is targeted at a Heroku branch", pr);
            return;
        }

        val files = repository.getPullRequestFiles(pr);
        val modifiesJava = files.stream().anyMatch(file -> !file.getFilename().contains("Tests") && file.getFilename().endsWith(".java"));
        if (modifiesJava) {
            val hasTests = files.stream().anyMatch(file -> file.getFilename().endsWith("Tests.java"));
            if (!hasTests) {
                log.info("Pull request {} does not have any tests", pr);
                if (!pr.isLabeledAs(CasLabels.LABEL_PENDING_NEEDS_TESTS)) {
                    repository.labelPullRequestAs(pr, CasLabels.LABEL_PENDING_NEEDS_TESTS);
                }
                repository.createStatusForFailure(pr, "Tests",
                    "Missing unit/integration/browser tests with adequate test coverage. "
                        + "Pull requests that lack sufficient tests will generally not be accepted.");
            } else {
                if (pr.isLabeledAs(CasLabels.LABEL_PENDING_NEEDS_TESTS)) {
                    repository.removeLabelFrom(pr, CasLabels.LABEL_PENDING_NEEDS_TESTS);
                }
                repository.createStatusForSuccess(pr, "Tests", "Good job! A positive pull request.");
            }
        }
    }

    private boolean processInvalidPullRequest(final PullRequest pr) {
        val count = repository.getPullRequestFiles(pr).stream()
            .filter(file -> {
                var fname = file.getFilename();
                return !fname.contains("src/test/java")
                    && !fname.endsWith(".html")
                    && !fname.endsWith(".js")
                    && !fname.endsWith(".jpg")
                    && !fname.endsWith(".jpeg")
                    && !fname.endsWith(".sh")
                    && !fname.endsWith(".txt")
                    && !fname.endsWith(".md")
                    && !fname.endsWith(".gif")
                    && !fname.endsWith(".css");
            })
            .count();

        if (count >= repository.getGitHubProperties().getMaximumChangedFiles()) {
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


    private boolean processLabelSeeMaintenancePolicy(final PullRequest pr) {
        if (!pr.isTargetedAtMasterBranch() && !pr.isLabeledAs(CasLabels.LABEL_SEE_MAINTENANCE_POLICY)
            && !pr.isTargetBranchOnHeroku() && !pr.isWorkInProgress()) {
            var milestones = repository.getActiveMilestones();
            val milestone = MonitoredRepository.getMilestoneForBranch(milestones, pr.getBase().getRef());
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
        if (!pr.isTargetBranchOnHeroku() && !pr.getBase().isRefMaster()
            && !pr.isLabeledAs(CasLabels.LABEL_PENDING_PORT_FORWARD)) {
            log.info("{} is targeted at a branch {} and should be ported forward to the master branch", pr, pr.getBase());
            repository.labelPullRequestAs(pr, CasLabels.LABEL_PENDING_PORT_FORWARD);
        }
    }

    private void processMilestoneAssignment(final PullRequest pr) {
        if (pr.getMilestone() == null && !pr.isTargetBranchOnHeroku()) {
            if (pr.isTargetedAtMasterBranch()) {
                val milestoneForMaster = repository.getMilestoneForMaster();
                milestoneForMaster.ifPresent(milestone -> {
                    log.info("{} will be assigned the master milestone {}", pr, milestone);
                    repository.getGitHub().setMilestone(pr, milestone);
                });
            } else {
                var milestones = repository.getActiveMilestones();
                val milestone = MonitoredRepository.getMilestoneForBranch(milestones, pr.getBase().getRef());
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
