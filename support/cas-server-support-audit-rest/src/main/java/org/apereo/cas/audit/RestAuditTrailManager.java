package org.apereo.cas.audit;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
import org.apereo.cas.util.HttpUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link RestAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RestAuditTrailManager implements AuditTrailManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAuditTrailManager.class);

    private final AuditActionContextJsonSerializer serializer;
    private final AuditRestProperties properties;

    public RestAuditTrailManager(final AuditRestProperties properties) {
        this.serializer = new AuditActionContextJsonSerializer();
        this.properties = properties;
    }

    @Override
    public void record(final AuditActionContext audit) {
        final String auditJson = this.serializer.toString(audit);
        if (StringUtils.isNotBlank(properties.getUrl())) {
            LOGGER.debug("Sending audit action context to REST endpoint [{}]", properties.getUrl());
            HttpUtils.executePost(properties.getUrl(), properties.getBasicAuthUsername(), properties.getBasicAuthPassword(), auditJson);
        } else {
            LOGGER.warn("No REST endpoint URL is defined to use for logging audit actions and context");
        }
    }
}
