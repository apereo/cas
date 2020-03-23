package org.apereo.cas.configuration.model.support.git.services;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;

import java.io.File;
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
     * Path to the SSH private key identity.
     */
    private String privateKeyPassphrase;

    /**
     * Password for the SSH private key.
     */
    private File privateKeyPath;

    /**
     * As with using SSH with public keys, an SSH session
     * with {@code ssh://user@example.com/repo.git}
     * must be specified to use password-secured SSH connections.
     */
    private String sshSessionPassword;

    /**
     * Timeout for git operations such as push and pull in seconds.
     */
    private String timeout = "PT10S";

    /**
     * Directory into which the repository would be cloned.
     */
    private File cloneDirectory = new File(FileUtils.getTempDirectory(), "cas-git-clone");
}
