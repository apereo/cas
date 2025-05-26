package org.apereo.cas.configuration.model;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.core.io.Resource;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SpringResourceProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
@ToString

public class SpringResourceProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 4142130961445546358L;

    /**
     * The location of the resource. Resources can be URLs, or
     * files found either on the classpath or outside somewhere
     * in the file system.
     * <p>
     * In the event the configured resource is a Groovy script, especially if the script is set to reload on changes,
     * you may need to adjust the total number of {@code inotify} instances.
     * On Linux, you may need to add the following line to {@code /etc/sysctl.conf}:
     * {@code fs.inotify.max_user_instances = 256}.
     * <p>
     * You can check the current value via {@code cat /proc/sys/fs/inotify/max_user_instances}.
     * <p>
     * In situations and scenarios where CAS is able to automatically watch the underlying resource
     * for changes and detect updates and modifications dynamically, you may be able to specify the following
     * setting as either an environment variable or system property with a value of {@code false} to disable
     * the resource watcher: {@code org.apereo.cas.util.io.PathWatcherService}.
     */
    @RequiredProperty
    private transient Resource location;
}
