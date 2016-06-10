package org.apereo.cas.configuration.model.support.saml.mdui;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * This is {@link SamlMetadataUIProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class SamlMetadataUIProperties {
    private int startDelay;
    private int repeatInterval;
    private String parameter;
    private long maxValidity;
    private boolean requireSignedRoot;
    private boolean requireValidMetadata;
    private List<String> resources;

    public List<String> getResources() {
        return resources;
    }

    public void setResources(final List<String> resources) {
        this.resources = resources;
    }

    public boolean isRequireValidMetadata() {
        return requireValidMetadata;
    }

    public void setRequireValidMetadata(final boolean requireValidMetadata) {
        this.requireValidMetadata = requireValidMetadata;
    }
    
    public String getParameter() {
        return parameter;
    }

    public void setParameter(final String parameter) {
        this.parameter = parameter;
    }

    public int getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(final int startDelay) {
        this.startDelay = startDelay;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(final int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public long getMaxValidity() {
        return maxValidity;
    }

    public void setMaxValidity(final long maxValidity) {
        this.maxValidity = maxValidity;
    }

    public boolean isRequireSignedRoot() {
        return requireSignedRoot;
    }

    public void setRequireSignedRoot(final boolean requireSignedRoot) {
        this.requireSignedRoot = requireSignedRoot;
    }

    public static class Fixed {
        private List<String> resources;

        public List<String> getResources() {
            return resources;
        }

        public void setResources(final List<String> resources) {
            this.resources = resources;
        }
    }

    public static class Dynamic {

    }
}
