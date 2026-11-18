package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditActionCount;
import org.apereo.inspektr.audit.AuditSummaryQuery;
import org.hjson.JsonValue;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

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

    private final AuditActionContextJsonSerializer serializer;

    private final AuditRestProperties properties;

    public RestAuditTrailManager(final AuditActionContextJsonSerializer serializer, final AuditRestProperties properties) {
        super(properties.isAsynchronous());
        this.serializer = serializer;
        this.properties = properties;
    }

    @Override
    public void saveAuditRecord(final AuditActionContext audit) {
        HttpResponse response = null;
        try {
            val auditJson = serializer.toString(audit);
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
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
    public List<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        HttpResponse response = null;
        try {
            val date = (TemporalAccessor) whereClause.get(WhereClauseFields.DATE);
            LOGGER.debug("Sending query to audit REST endpoint to fetch records from [{}]", date);
            val parameters = CollectionUtils.<String, String>wrap("date", date.toString());
            if (whereClause.containsKey(WhereClauseFields.PRINCIPAL)) {
                parameters.put("principal", whereClause.get(WhereClauseFields.PRINCIPAL).toString());
            }
            if (whereClause.containsKey(WhereClauseFields.COUNT)) {
                parameters.put("count", whereClause.get(WhereClauseFields.COUNT).toString());
            }
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(properties.getUrl())
                .parameters(parameters)
                .headers(properties.getHeaders())
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && response.getCode() == HttpStatus.SC_OK) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    return readAuditRecords(JsonValue.readHjson(result).toString());
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return List.of();
    }

    /**
     * Reads the audit records the endpoint returned. Records are posted with type information, so a backend that hands
     * them back as it received them returns {@code @class} properties and typed collections; those are read with the
     * serializer that wrote them. Records without type information, and a list wrapped as
     * {@code ["java.util.ArrayList", [...]]}, are read as well.
     *
     * @param json the response body
     * @return the audit records
     */
    protected List<AuditActionContext> readAuditRecords(final String json) {
        val root = MAPPER.readTree(json);
        val typedList = root.isArray() && root.size() == 2 && root.get(0).isString() && root.get(1).isArray();
        val records = new ArrayList<AuditActionContext>();
        (typedList ? root.get(1) : root).values().forEach(node -> records.add(node.has("@class")
            ? serializer.from(node.toString())
            : MAPPER.readValue(node.toString(), AuditActionContext.class)));
        return records;
    }

    /**
     * Asks the audit service for its counts with a {@code GET} to the audit URL, passing {@code summary=hourly},
     * {@code from} and {@code to} as ISO dates, {@code actions} as a comma-separated list and, to count per target too,
     * {@code groupBy=target}. The service answers with a list of {@code hour}, {@code action} and {@code count}, and
     * {@code target} when grouped by target; a service that answers anything else does not support summaries.
     *
     * @param query the period and actions to count
     * @return the counts, or empty when the service does not summarize
     */
    @Override
    public Optional<List<AuditActionCount>> summarize(final AuditSummaryQuery query) {
        HttpResponse response = null;
        try {
            val parameters = CollectionUtils.<String, String>wrap(
                "summary", "hourly",
                "from", query.from().toString(),
                "to", query.to().toString(),
                "actions", query.actions().stream().sorted().collect(Collectors.joining(",")));
            if (query.byTarget()) {
                parameters.put("groupBy", "target");
            }
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(properties.getUrl())
                .parameters(parameters)
                .headers(properties.getHeaders())
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && response.getCode() == HttpStatus.SC_OK) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    return readAuditSummary(IOUtils.toString(content, StandardCharsets.UTF_8));
                }
            }
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }

    protected Optional<List<AuditActionCount>> readAuditSummary(final String json) {
        val root = MAPPER.readTree(JsonValue.readHjson(json).toString());
        if (!root.isArray()) {
            return Optional.empty();
        }
        val counts = new ArrayList<AuditActionCount>();
        for (val node : root.values()) {
            if (!node.hasNonNull("hour") || !node.hasNonNull("action") || !node.hasNonNull("count")) {
                return Optional.empty();
            }
            counts.add(new AuditActionCount(LocalDateTime.parse(node.path("hour").asString()),
                node.path("action").asString(), node.path("count").asLong(),
                node.hasNonNull("target") ? node.path("target").asString() : null));
        }
        return Optional.of(counts);
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
                .headers(properties.getHeaders())
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
