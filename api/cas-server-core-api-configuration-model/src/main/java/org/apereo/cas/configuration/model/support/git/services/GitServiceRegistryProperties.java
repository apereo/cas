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

    public GitServiceRegistryProperties() {
        setCloneDirectory(new File(FileUtils.getTempDirectory(), "cas-service-registry"));
    }
}
