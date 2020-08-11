package org.apereo.cas.git;

import org.apereo.cas.configuration.model.support.git.services.BaseGitProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.ChainingCredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.NetRCCredentialsProvider;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
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
@Builder
@Slf4j
public class GitRepositoryBuilder {
    @Builder.Default
    private final List<CredentialsProvider> credentialsProviders = new ArrayList<>(0);

    private final String repositoryUri;

    private final File repositoryDirectory;

    private final String branchesToClone;

    private final String activeBranch;

    private final String privateKeyPath;

    private final String privateKeyPassphrase;

    private final String sshSessionPassword;

    private final long timeoutInSeconds;

    private final boolean signCommits;

    private static String getBranchPath(final String branchName) {
        return "refs/heads/" + branchName;
    }

    /**
     * New instance of git repository builder.
     *
     * @param props the registry
     * @return the git repository builder
     */
    @SneakyThrows
    public static GitRepositoryBuilder newInstance(final BaseGitProperties props) {
        val builder = GitRepositoryBuilder.builder()
            .repositoryUri(props.getRepositoryUrl())
            .activeBranch(props.getActiveBranch())
            .branchesToClone(props.getBranchesToClone())
            .repositoryDirectory(props.getCloneDirectory())
            .privateKeyPassphrase(props.getPrivateKeyPassphrase())
            .sshSessionPassword(props.getSshSessionPassword())
            .timeoutInSeconds(Beans.newDuration(props.getTimeout()).toSeconds())
            .signCommits(props.isSignCommits());
        if (StringUtils.hasText(props.getUsername())) {
            val providers = CollectionUtils.wrapList(
                new UsernamePasswordCredentialsProvider(props.getUsername(), props.getPassword()),
                new NetRCCredentialsProvider());
            builder.credentialsProviders(providers);
        }
        if (props.getPrivateKeyPath() != null) {
            builder.privateKeyPath(props.getPrivateKeyPath().getCanonicalPath());
        }
        return builder.build();
    }

    /**
     * Build transport config callback.
     *
     * @return the transport config callback
     */
    protected TransportConfigCallback buildTransportConfigCallback() {
        val sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(final OpenSshConfig.Host host, final Session session) {
                if (StringUtils.hasText(sshSessionPassword)) {
                    session.setPassword(sshSessionPassword);
                }
            }

            @Override
            protected JSch createDefaultJSch(final FS fs) throws JSchException {
                val defaultJSch = super.createDefaultJSch(fs);
                if (StringUtils.hasText(privateKeyPath)) {
                    defaultJSch.addIdentity(privateKeyPath, privateKeyPassphrase);
                }
                return defaultJSch;
            }
        };
        return transport -> {
            if (transport instanceof SshTransport) {
                val sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
        };
    }

    /**
     * Build git repository.
     *
     * @return the git repository
     */
    @SuppressWarnings("java:S2095")
    public GitRepository build() {
        try {
            val transportCallback = buildTransportConfigCallback();
            val providers = this.credentialsProviders.toArray(CredentialsProvider[]::new);
            if (this.repositoryDirectory.exists()) {
                LOGGER.debug("Using existing repository at [{}]", this.repositoryDirectory);
                return getExistingGitRepository(transportCallback);
            }
            return cloneGitRepository(transportCallback, providers);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private GitRepository cloneGitRepository(final TransportConfigCallback transportCallback,
                                             final CredentialsProvider[] providers) throws Exception {
        val cloneCommand = Git.cloneRepository()
            .setProgressMonitor(new LoggingGitProgressMonitor())
            .setURI(this.repositoryUri)
            .setDirectory(this.repositoryDirectory)
            .setBranch(this.activeBranch)
            .setTimeout((int) this.timeoutInSeconds)
            .setTransportConfigCallback(transportCallback)
            .setCredentialsProvider(new ChainingCredentialsProvider(providers));

        if (!StringUtils.hasText(this.branchesToClone) || "*".equals(branchesToClone)) {
            cloneCommand.setCloneAllBranches(true);
        } else {
            cloneCommand.setBranchesToClone(StringUtils.commaDelimitedListToSet(this.branchesToClone)
                .stream()
                .map(GitRepositoryBuilder::getBranchPath)
                .collect(Collectors.toList()));
        }
        LOGGER.debug("Cloning repository at [{}] with branch [{}]", this.repositoryDirectory, this.activeBranch);
        return new GitRepository(cloneCommand.call(), credentialsProviders,
            transportCallback, this.timeoutInSeconds, this.signCommits);
    }


    private GitRepository getExistingGitRepository(final TransportConfigCallback transportCallback) throws Exception {
        val git = Git.open(this.repositoryDirectory);
        LOGGER.debug("Checking out the branch [{}] at [{}]", this.activeBranch, this.repositoryDirectory);
        git.checkout()
            .setName(this.activeBranch)
            .call();
        return new GitRepository(git, this.credentialsProviders, transportCallback,
            this.timeoutInSeconds, this.signCommits);
    }
}
