package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatExtendedAccessLogProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Accessors(chain = true)
@Setter
public class CasEmbeddedApacheTomcatExtendedAccessLogProperties implements Serializable {

    private static final long serialVersionUID = 6738161402499196038L;

    /**
     * Flag to indicate whether extended log facility is enabled.
     */
    private boolean enabled;

    /**
     * String representing extended log pattern.
     */
    private String pattern = "c-ip s-ip cs-uri sc-status time x-threadname x-H(secure) x-H(remoteUser)";

    /**
     * File name suffix for extended log.
     */
    private String suffix = ".log";

    /**
     * File name prefix for extended log.
     */
    private String prefix = "localhost_access_extended";

    /**
     * Directory name for extended log.
     */
    private String directory;
}
