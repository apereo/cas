package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apereo.inspektr.audit.AuditActionContext;
import org.hjson.JsonValue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link RestAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RestAuditTrailManager extends AbstractAuditTrailManager {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final AuditActionContextJsonSerializer serializer = new AuditActionContextJsonSerializer();
    private final AuditRestProperties properties;

    public RestAuditTrailManager(final AuditRestProperties properties) {
        super(properties.isAsynchronous());
        this.properties = properties;
    }

    @Override
    public void saveAuditRecord(final AuditActionContext audit) {
        HttpResponse response = null;
        try {
            val auditJson = serializer.toString(audit);
            LOGGER.trace("Sending audit action context to REST endpoint [{}]", properties.getUrl());
            response = HttpUtils.executePost(properties.getUrl(), properties.getBasicAuthUsername(), properties.getBasicAuthPassword(), auditJson);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void removeAll() {
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        HttpResponse response = null;
        try {
            LOGGER.debug("Sending query to audit REST endpoint to fetch records from [{}]", localDate);
            response = HttpUtils.executeGet(properties.getUrl(), properties.getBasicAuthUsername(),
                properties.getBasicAuthPassword(), CollectionUtils.wrap("date", String.valueOf(localDate.toEpochDay())));
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                final TypeReference<Set<AuditActionContext>> values = new TypeReference<>() {
                };
                return MAPPER.readValue(JsonValue.readHjson(result).toString(), values);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return new HashSet<>(0);
    }
}
