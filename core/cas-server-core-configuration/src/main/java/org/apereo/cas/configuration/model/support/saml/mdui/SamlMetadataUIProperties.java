package org.apereo.cas.configuration.model.support.saml.mdui;

import org.apereo.cas.configuration.support.Beans;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link SamlMetadataUIProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class SamlMetadataUIProperties {
    private String startDelay = "PT30S";
    private String repeatInterval = "PT2M";
    private String parameter = "entityId";
    private long maxValidity;
    private boolean requireSignedRoot;
    private boolean requireValidMetadata = true;
    private List<String> resources = Arrays.asList("classpath:/sp-metadata::classpath:/pub.key,"
        + "http://md.incommon.org/InCommon/InCommon-metadata.xml::classpath:/inc-md-pub.key");

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

    public long getStartDelay() {
        return Beans.newDuration(startDelay).toMillis();
    }

    public void setStartDelay(final String startDelay) {
        this.startDelay = startDelay;
    }

    public long getRepeatInterval() {
        return Beans.newDuration(repeatInterval).toMillis();
    }

    public void setRepeatInterval(final String repeatInterval) {
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
    
}
