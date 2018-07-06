package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apereo.inspektr.audit.AuditActionContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is {@link RestAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestAuditTrailManager extends AbstractAuditTrailManager {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AuditActionContextJsonSerializer serializer = new AuditActionContextJsonSerializer();
    private final AuditRestProperties properties;

    @Override
    public void saveAuditRecord(final AuditActionContext audit) {
        val auditJson = serializer.toString(audit);
        LOGGER.debug("Sending audit action context to REST endpoint [{}]", properties.getUrl());
        HttpUtils.executePost(properties.getUrl(), properties.getBasicAuthUsername(), properties.getBasicAuthPassword(), auditJson);
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        try {
            LOGGER.debug("Sending query to audit REST endpoint to fetch records from [{}]", localDate);
            val response = HttpUtils.executeGet(properties.getUrl(), properties.getBasicAuthUsername(),
                properties.getBasicAuthPassword(), CollectionUtils.wrap("date", String.valueOf(localDate.toEpochDay())));
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                final TypeReference<Set<AuditActionContext>> values = new TypeReference<>() {
                };
                return MAPPER.readValue(result, values);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashSet<>(0);
    }

}
