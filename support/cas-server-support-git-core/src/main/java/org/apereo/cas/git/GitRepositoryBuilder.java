package org.apereo.cas.git;

import org.apereo.cas.configuration.model.support.git.services.BaseGitProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

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
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.NetRCCredentialsProvider;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.http.JDKHttpConnectionFactory;
import org.eclipse.jgit.transport.http.apache.HttpClientConnectionFactory;
import org.eclipse.jgit.util.FS;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

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

    private final Resource repositoryDirectory;

    private final String branchesToClone;

    private final String activeBranch;

    private final String privateKeyPath;

    private final String privateKeyPassphrase;

    private final String sshSessionPassword;

    private final long timeoutInSeconds;

    private final boolean signCommits;

    private final boolean strictHostKeyChecking;

    private final boolean clearExistingIdentities;

    private final BaseGitProperties.HttpClientTypes httpClientType;

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
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val builder = GitRepositoryBuilder.builder()
            .repositoryUri(resolver.resolve(props.getRepositoryUrl()))
            .activeBranch(resolver.resolve(props.getActiveBranch()))
            .branchesToClone(props.getBranchesToClone())
            .repositoryDirectory(props.getCloneDirectory().getLocation())
            .privateKeyPassphrase(props.getPrivateKeyPassphrase())
            .sshSessionPassword(props.getSshSessionPassword())
            .timeoutInSeconds(Beans.newDuration(props.getTimeout()).toSeconds())
            .signCommits(props.isSignCommits())
            .clearExistingIdentities(props.isClearExistingIdentities())
            .strictHostKeyChecking(props.isStrictHostKeyChecking())
            .httpClientType(props.getHttpClientType());
        if (StringUtils.hasText(props.getUsername())) {
            val providers = CollectionUtils.wrapList(
                new UsernamePasswordCredentialsProvider(props.getUsername(), props.getPassword()),
                new NetRCCredentialsProvider());
            builder.credentialsProviders(providers);
        }
        if (props.getPrivateKey().getLocation() != null) {
            val resource = ResourceUtils.prepareClasspathResourceIfNeeded(props.getPrivateKey().getLocation());
            if (resource != null && resource.exists()) {
                builder.privateKeyPath(resource.getFile().getCanonicalPath());
            }
        }
        return builder.build();
    }

    /**
     * Build transport config callback.
     *
     * @return the transport config callback
     */
    protected TransportConfigCallback buildTransportConfigCallback() {
        return transport -> {
            if (transport instanceof SshTransport) {
                val sshSessionFactory = new JschConfigSessionFactory() {
                    @Override
                    protected void configure(final OpenSshConfig.Host host, final Session session) {
                        if (StringUtils.hasText(sshSessionPassword)) {
                            session.setPassword(sshSessionPassword);
                        }
                        if (!strictHostKeyChecking) {
                            session.setConfig("StrictHostKeyChecking", "no");
                        }
                    }

                    @Override
                    protected JSch createDefaultJSch(final FS fs) throws JSchException {
                        val defaultJSch = super.createDefaultJSch(fs);
                        if (clearExistingIdentities) {
                            defaultJSch.removeAllIdentity();
                        }

                        if (StringUtils.hasText(privateKeyPath)) {
                            defaultJSch.addIdentity(privateKeyPath, privateKeyPassphrase);
                        }
                        return defaultJSch;
                    }
                };
                val sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
            if (transport instanceof HttpTransport) {
                if (httpClientType == BaseGitProperties.HttpClientTypes.JDK) {
                    HttpTransport.setConnectionFactory(new JDKHttpConnectionFactory());
                } else if (httpClientType == BaseGitProperties.HttpClientTypes.HTTP_CLIENT) {
                    HttpTransport.setConnectionFactory(new HttpClientConnectionFactory());
                }
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
            LoggingUtils.error(LOGGER, e);
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private GitRepository cloneGitRepository(final TransportConfigCallback transportCallback,
                                             final CredentialsProvider[] providers) throws Exception {
        val cloneCommand = Git.cloneRepository()
            .setProgressMonitor(new LoggingGitProgressMonitor())
            .setURI(this.repositoryUri)
            .setDirectory(this.repositoryDirectory.getFile())
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
        LOGGER.debug("Cloning repository to [{}] with branch [{}]", this.repositoryDirectory, this.activeBranch);
        return new GitRepository(cloneCommand.call(), credentialsProviders,
            transportCallback, this.timeoutInSeconds, this.signCommits);
    }


    private GitRepository getExistingGitRepository(final TransportConfigCallback transportCallback) throws Exception {
        val git = Git.open(this.repositoryDirectory.getFile());
        LOGGER.debug("Checking out the branch [{}] at [{}]", this.activeBranch, this.repositoryDirectory);
        git.checkout()
            .setName(this.activeBranch)
            .call();
        return new GitRepository(git, this.credentialsProviders, transportCallback,
            this.timeoutInSeconds, this.signCommits);
    }
}
