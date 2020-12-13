package org.apereo.cas.configuration.model.support.git.services;

import org.apereo.cas.configuration.model.SpringResourceProperties;
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
    private String repositoryUrl;

    /**
     * The branch to checkout and activate.
     */
    private String activeBranch = "master";

    /**
     * If the repository is to be cloned,
     * this will allow the list of branches to be fetched
     * separated by commas.
     */
    private String branchesToClone = "master";

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
     * and identity should only be limitd to those loaded from given keys.
     */
    private boolean clearExistingIdentities;

    /**
     * Timeout for git operations such as push and pull in seconds.
     */
    private String timeout = "PT10S";

    /**
     * Directory into which the repository would be cloned.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties cloneDirectory = new SpringResourceProperties();
}
