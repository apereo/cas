package org.apereo.cas.configuration.model.support.saml.mdui;

import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link SamlMetadataUIProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlMetadataUIProperties {

    /**
     * The parameter name that indicates the entity id of the service provider
     * submitted to CAS.
     */
    private String parameter = "entityId";

    /**
     * If specified, creates a validity filter on the metadata to check for
     * metadata freshness based on the max validity. Value is specified in seconds.
     */
    private long maxValidity;

    /**
     * When parsing metadata, whether the root element is required to be signed.
     */
    private boolean requireSignedRoot;
    /**
     * Whether valid metadata is required when parsing metadata.
     */
    private boolean requireValidMetadata = true;

    /**
     * Metadata resources to load and parse through based on the incoming entity id
     * in order to locate MDUI. Resources can be classpath/file/http resources.
     * If each metadata resource has a signing certificate, they can be added onto the resource with a <code>::</code>
     * separator. Example: <code>classpath:/sp-metadata.xml::classpath:/pub.key</code>.
     */
    private List<String> resources = Arrays.asList("classpath:/sp-metadata::classpath:/pub.key,"
            + "http://md.incommon.org/InCommon/InCommon-metadata.xml::classpath:/inc-md-pub.key");

    /**
     * Scheduler settings to indicate how often is metadata reloaded.
     */
    @NestedConfigurationProperty
    private SchedulingProperties schedule = new SchedulingProperties();

    public SamlMetadataUIProperties() {
        schedule.setEnabled(true);
        schedule.setStartDelay("PT30S");
        schedule.setRepeatInterval("PT2M");
    }

    public SchedulingProperties getSchedule() {
        return schedule;
    }

    public void setSchedule(final SchedulingProperties schedule) {
        this.schedule = schedule;
    }

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
