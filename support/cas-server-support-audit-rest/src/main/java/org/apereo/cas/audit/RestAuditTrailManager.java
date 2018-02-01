package org.apereo.cas.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.util.Assert;

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
public class RestAuditTrailManager implements AuditTrailManager {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Setter
    private boolean asynchronous = true;
    
    private final AuditActionContextJsonSerializer serializer;
    private final AuditRestProperties properties;

    public RestAuditTrailManager(final AuditRestProperties properties) {
        this.serializer = new AuditActionContextJsonSerializer();
        this.properties = properties;
        Assert.notNull(properties.getUrl(), "REST endpoint url cannot be null");
    }
    
    @Override
    public void record(final AuditActionContext audit) {
        final Runnable task = () -> {
            final String auditJson = serializer.toString(audit);
            LOGGER.debug("Sending audit action context to REST endpoint [{}]", properties.getUrl());
            HttpUtils.executePost(properties.getUrl(), properties.getBasicAuthUsername(), properties.getBasicAuthPassword(), auditJson);
        };

        if (this.asynchronous) {
            this.executorService.execute(task);
        } else {
            task.run();
        }
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
