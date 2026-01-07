package org.apereo.inspektr.audit;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apereo.cas.util.thread.Cleanable;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * An interface used to make an audit trail record.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0
 */
@FunctionalInterface
public interface AuditTrailManager extends Cleanable {
    Logger LOG = LoggerFactory.getLogger(AuditTrailManager.class);

    /**
     * Enumerate auditable known fields.
     */
    enum AuditableFields {
        /**
         * Who field.
         */
        WHO,
        /**
         * What (resource) field.
         */
        WHAT,
        /**
         * Action that was performed.
         */
        ACTION,
        /**
         * Application field.
         */
        APPLICATION,
        /**
         * When this action was performed?.
         */
        WHEN,
        /**
         * User agent extracted from the browser.
         */
        USER_AGENT,
        /**
         * Client IP extracted from the browser.
         */
        CLIENT_IP,
        /**
         * Server IP extracted from the browser.
         */
        SERVER_IP,
        /**
         * Geo-Location extracted from the browser.
         */
        GEO_LOCATION,
        /**
         * Device/Browser fingerprint extracted from the browser.
         */
        DEVICE_FINGERPRINT,
        /**
         * Headers attached to the request.
         */
        HEADERS,
        /**
         * Tenant information/id.
         */
        TENANT
    }

    /**
     * Enumerate fields used to build where clauses.
     */
    enum WhereClauseFields {
        /**
         * Limit results by date/time.
         */
        DATE,
        /**
         * Limit results by principal/subject.
         */
        PRINCIPAL,
        /**
         * Limit the results by a total count of records retrieved.
         */
        COUNT
    }

    /**
     * ObjectMapper instance.
     */
    ObjectMapper MAPPER = JsonMapper.builderWithJackson2Defaults()
        .findAndAddModules()
        .changeDefaultPropertyInclusion(handler -> handler.withValueInclusion(JsonInclude.Include.NON_EMPTY)
            .withContentInclusion(JsonInclude.Include.NON_EMPTY))
        .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .build();

    /**
     * Convert object to JSON.
     *
     * @param arg the arg
     * @return the string
     */
    static String toJson(@Nullable final Object arg) {
        try {
            return MAPPER.writeValueAsString(arg);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "{}";
    }

    /**
     * Make an audit trail record. Implementations could use any type of back end medium to serialize audit trail
     * data i.e. RDBMS, log file, IO stream, SMTP, JMS queue or what ever else imaginable.
     * <p>
     * This concept is somewhat similar to log4j Appender.
     *
     * @param auditActionContext the audit action context
     */
    void record(AuditActionContext auditActionContext);

    /**
     * Gets audit records since.
     *
     * @param whereClause the where clause
     * @return the audit records since
     */
    default List<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        return List.of();
    }

    /**
     * Remove all.
     */
    default void removeAll() {
    }

    /**
     * Sets audit format.
     *
     * @param auditFormat the audit format
     */
    default void setAuditFormat(final AuditTrailManager.AuditFormats auditFormat) {
    }

    enum AuditFormats {
        /**
         * Return an audited object by invoking {@link Object#toString()} on it.
         */
        DEFAULT {
            @Override
            public String serialize(final Object object) {
                return object.toString();
            }
        },
        /**
         * Return an audited object by transforming it into JSON.
         */
        JSON {
            @Override
            public String serialize(final Object object) {
                return AuditTrailManager.toJson(object);
            }
        };

        /**
         * Serialize object.
         *
         * @param object the object
         * @return the string
         */
        public abstract String serialize(Object object);
    }
}
