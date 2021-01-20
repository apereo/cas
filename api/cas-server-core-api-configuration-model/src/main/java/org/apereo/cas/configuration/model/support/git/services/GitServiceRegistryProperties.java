package org.apereo.cas.configuration.model.support.git.services;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;

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
@JsonFilter("GitServiceRegistryProperties")
public class GitServiceRegistryProperties extends BaseGitProperties {

    /**
     * Default name used for git service registry clone directory.
     */
    public static final String DEFAULT_CAS_SERVICE_REGISTRY_NAME = "cas-service-registry";

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
        val location = new FileSystemResource(new File(FileUtils.getTempDirectory(), DEFAULT_CAS_SERVICE_REGISTRY_NAME));
        getCloneDirectory().setLocation(location);
    }
}
