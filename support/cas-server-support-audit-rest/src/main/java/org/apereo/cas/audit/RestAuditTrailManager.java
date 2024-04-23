package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apereo.inspektr.audit.AuditActionContext;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RestAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RestAuditTrailManager extends AbstractAuditTrailManager {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

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
            val headers = CollectionUtils.<String, String>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE,
                "userAgent", StringUtils.defaultIfBlank(audit.getClientInfo().getUserAgent(), "N/A"));
            headers.putAll(properties.getHeaders());

            LOGGER.trace("Sending audit action context to REST endpoint [{}]", properties.getUrl());
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(properties.getUrl())
                .entity(auditJson)
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        HttpResponse response = null;
        try {
            val localDate = (LocalDate) whereClause.get(WhereClauseFields.DATE);
            LOGGER.debug("Sending query to audit REST endpoint to fetch records from [{}]", localDate);
            val parameters = CollectionUtils.<String, String>wrap("date", String.valueOf(localDate.toEpochDay()));
            FunctionUtils.doIf(whereClause.containsKey(WhereClauseFields.PRINCIPAL),
                    c -> parameters.put("principial", whereClause.get(WhereClauseFields.PRINCIPAL).toString()))
                .accept(whereClause);
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(properties.getUrl())
                .parameters(parameters)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && response.getCode() == HttpStatus.SC_OK) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    val values = new TypeReference<Set<AuditActionContext>>() {
                    };
                    return MAPPER.readValue(JsonValue.readHjson(result).toString(), values);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new HashSet<>(0);
    }

    @Override
    public void removeAll() {
        HttpResponse response = null;
        try {
            LOGGER.debug("Sending query to audit REST endpoint to delete records");
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(properties.getUrl())
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && response.getCode() == HttpStatus.SC_OK) {
                LOGGER.debug("Deleted audit records successfully");
            }
        } finally {
            HttpUtils.close(response);
        }
    }
}
