package org.apereo.cas.git;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.ChainingCredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link GitRepositoryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class GitRepositoryBuilder {
    private final String repositoryUri;

    private File repositoryDirectory;

    private String branchesToClone;
    private String activeBranch;

    private final List<CredentialsProvider> credentialsProviders = new ArrayList<>();

    /**
     * Location of the repository, or where the repository should be cloned.
     *
     * @param directory the directory
     * @return the git repository builder
     */
    public GitRepositoryBuilder repositoryDirectory(final File directory) {
        this.repositoryDirectory = directory;
        return this;
    }

    /**
     * Branches to clone.
     *
     * @param value the value
     * @return the git repository builder
     */
    public GitRepositoryBuilder branchesToClone(final String value) {
        this.branchesToClone = value;
        return this;
    }

    /**
     * Active branch to checkout and use.
     *
     * @param value the value
     * @return the git repository builder
     */
    public GitRepositoryBuilder activeBranch(final String value) {
        this.activeBranch = value;
        return this;
    }

    /**
     * Credential provider for repositories that require access.
     *
     * @param username the username
     * @param password the password
     * @return the git repository builder
     */
    public GitRepositoryBuilder credentialProvider(final String username, final String password) {
        if (StringUtils.hasText(username)) {
            this.credentialsProviders.add(new UsernamePasswordCredentialsProvider(username, password));
        }
        return this;
    }

    /**
     * Build git repository.
     *
     * @return the git repository
     */
    public GitRepository build() {
        try {
            val providers = this.credentialsProviders.toArray(CredentialsProvider[]::new);
            if (this.repositoryDirectory.exists()) {
                val git = Git.open(this.repositoryDirectory);
                git.checkout()
                    .setName(this.activeBranch)
                    .call();
                return new GitRepository(git, this.credentialsProviders);
            }

            val cloneCommand = Git.cloneRepository()
                .setProgressMonitor(new LoggingGitProgressMonitor())
                .setURI(this.repositoryUri)
                .setDirectory(this.repositoryDirectory)
                .setBranch(this.activeBranch)
                .setCredentialsProvider(new ChainingCredentialsProvider(providers));

            if (StringUtils.hasText(this.branchesToClone) || "*".equals(branchesToClone)) {
                cloneCommand.setCloneAllBranches(true);
            } else {
                cloneCommand.setBranchesToClone(StringUtils.commaDelimitedListToSet(this.branchesToClone)
                    .stream()
                    .map(GitRepositoryBuilder::getBranchPath)
                    .collect(Collectors.toList()));
            }
            return new GitRepository(cloneCommand.call(), credentialsProviders);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private static String getBranchPath(final String branchName) {
        return "refs/heads/" + branchName;
    }
}
