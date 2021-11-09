package org.apereo.cas.configuration.model.support.git.services;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link BaseGitProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-git-core")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseGitProperties implements Serializable {
    private static final long serialVersionUID = 4194689836396653458L;

    /**
     * The address of the git repository.
     * Could be a URL or a file-system path.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String repositoryUrl;

    /**
     * The branch to checkout and activate, defaults to {@code master}.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String activeBranch = "master";

    /**
     * If the repository is to be cloned,
     * this will allow a select list of branches to be fetched.
     * List the branch names separated by commas or use {@code *} to clone all branches.
     * Defaults to all branches.
     */
    @RequiredProperty
    private String branchesToClone = "*";

    /**
     * Username used to access or push to the repository.
     */
    private String username;

    /**
     * Password used to access or push to the repository.
     */
    private String password;

    /**
     * Decide whether changes should be pushed back into the remote repository.
     */
    private boolean pushChanges;

    /**
     * Whether or not commits should be signed.
     */
    private boolean signCommits;

    /**
     * Password for the SSH private key.
     */
    private String privateKeyPassphrase;

    /**
     * Path to the SSH private key identity.
     * Must be a resource that can resolve to an absolute file on disk due to Jsch library needing String path.
     * Classpath resource would work if file on disk rather than inside archive.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties privateKey = new SpringResourceProperties();

    /**
     * As with using SSH with public keys, an SSH session
     * with {@code ssh://user@example.com/repo.git}
     * must be specified to use password-secured SSH connections.
     */
    private String sshSessionPassword;

    /**
     * Whether on not to turn on strict host key checking.
     * true will be "yes", false will be "no", "ask" not supported.
     */
    private boolean strictHostKeyChecking = true;

    /**
     * When establishing an ssh session, determine if default
     * identities loaded on the machine should be excluded/removed
     * and identity should only be limited to those loaded from given keys.
     */
    private boolean clearExistingIdentities;

    /**
     * Timeout for git operations such as push and pull in seconds.
     */
    @DurationCapable
    private String timeout = "PT10S";

    /**
     * Directory into which the repository would be cloned.
     */
    @NestedConfigurationProperty
    @RequiredProperty
    private SpringResourceProperties cloneDirectory = new SpringResourceProperties();

    /**
     * Implementation of HTTP client to use when doing git operations via http/https.
     * The jgit library sets the connection factory statically (globally) so this property should
     * be set to the same value for all git repositories (services, saml, etc). Not doing
     * so might result in one connection factory being used for clone and another for subsequent
     * fetches.
     */
    private HttpClientTypes httpClientType = HttpClientTypes.JDK;

    /**
     * The jgit library supports multiple HTTP client implementations.
     */
    public enum HttpClientTypes {
        /**
         * Built-in JDK http/https client.
         */
        JDK,
        /**
         * Apache HTTP Client http/https client.
         */
        HTTP_CLIENT
    }

}
