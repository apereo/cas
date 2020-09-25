package org.apereo.cas.configuration.model.support.git.services;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * This is {@link GitServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-git-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class GitServiceRegistryProperties extends BaseGitProperties {
    private static final long serialVersionUID = 4194689836396653458L;

    /**
     * Root directory in the git repository
     * structure to track service definition files. This might be most useful
     * if the git repository is tasked with other types of files and configurations
     * and allowing a separate root directory for service
     * definitions provide a clean separation between services files and everything else.
     * This setting may work in concert with {@link #isGroupByType()}.
     * If left blank, the root folder of the git repository itself
     * is used as the root directory for service definitions.
     */
    private String rootDirectory;

    /**
     * Determine whether service definitions in the
     * git repository should be located/stored in groups and
     * separate folder structures based on the service type.
     *
     * @see #getRootDirectory()
     */
    private boolean groupByType = true;


    public GitServiceRegistryProperties() {
        setCloneDirectory(new File(FileUtils.getTempDirectory(), "cas-service-registry"));
    }
}
