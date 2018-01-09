package org.apereo.cas.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link RestAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RestAuditTrailManager implements AuditTrailManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAuditTrailManager.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final AuditActionContextJsonSerializer serializer;
    private final AuditRestProperties properties;

    public RestAuditTrailManager(final AuditRestProperties properties) {
        this.serializer = new AuditActionContextJsonSerializer();
        this.properties = properties;
        Assert.notNull(properties.getUrl());
    }

    @Override
    public void record(final AuditActionContext audit) {
        final String auditJson = this.serializer.toString(audit);
        LOGGER.debug("Sending audit action context to REST endpoint [{}]", properties.getUrl());
        HttpUtils.executePost(properties.getUrl(), properties.getBasicAuthUsername(), properties.getBasicAuthPassword(), auditJson);
    }

    @Override
    public Set<AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        try {
            LOGGER.debug("Sending query to audit REST endpoint to fetch records from [{}]", localDate);
            final HttpResponse response = HttpUtils.executeGet(properties.getUrl(), CollectionUtils.wrap("date", localDate.toEpochDay()));
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                final String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                final TypeReference<Set<AuditActionContext>> values = new TypeReference<Set<AuditActionContext>>() {
                };
                return MAPPER.readValue(result, values);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashSet<>(0);
    }
}
