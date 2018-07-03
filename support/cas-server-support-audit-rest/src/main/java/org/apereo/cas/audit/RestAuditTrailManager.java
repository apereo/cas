package org.apereo.cas.audit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
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
public class RestAuditTrailManager implements AuditTrailManager {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .registerModule(new SimpleModule().setMixInAnnotation(AuditActionContext.class, AbstractAuditActionContextMixin.class));

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Setter
    private boolean asynchronous = true;

    private final AuditActionContextJsonSerializer serializer = new AuditActionContextJsonSerializer();
    private final AuditRestProperties properties;

    @Override
    public void record(final AuditActionContext audit) {
        final Runnable task = () -> {
            final var auditJson = serializer.toString(audit);
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
            final var response = HttpUtils.executeGet(properties.getUrl(), properties.getBasicAuthUsername(),
                properties.getBasicAuthPassword(), CollectionUtils.wrap("date", String.valueOf(localDate.toEpochDay())));
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                final var result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                final TypeReference<Set<AuditActionContext>> values = new TypeReference<>() {
                };
                return MAPPER.readValue(result, values);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashSet<>(0);
    }


    private abstract static class AbstractAuditActionContextMixin extends AuditActionContext {
        private static final long serialVersionUID = -7839084408338396531L;

        @JsonCreator
        AbstractAuditActionContextMixin(@JsonProperty("principal") final String principal,
                                        @JsonProperty("resourceOperatedUpon") final String resourceOperatedUpon,
                                        @JsonProperty("actionPerformed") final String actionPerformed,
                                        @JsonProperty("applicationCode") final String applicationCode,
                                        @JsonProperty("whenActionWasPerformed") final Date whenActionWasPerformed,
                                        @JsonProperty("clientIpAddress") final String clientIpAddress,
                                        @JsonProperty("serverIpAddress") final String serverIpAddress) {
            super(principal, resourceOperatedUpon, actionPerformed,
                applicationCode, whenActionWasPerformed,
                clientIpAddress, serverIpAddress);
        }
    }

}
