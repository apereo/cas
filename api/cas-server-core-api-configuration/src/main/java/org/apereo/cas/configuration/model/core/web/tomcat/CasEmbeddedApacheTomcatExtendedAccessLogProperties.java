package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatExtendedAccessLogProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
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

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }
}
