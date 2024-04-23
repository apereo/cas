package org.apereo.inspektr.audit.support;

import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.util.jpa.MapToJsonAttributeConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Implementation of {@link AuditTrailManager} to persist the
 * audit trail to the {@code AUDIT_TRAIL} table in the Oracle data base.
 * </p>
 * <pre>
 * CREATE TABLE COM_AUDIT_TRAIL
 * (
 *  AUD_USER      VARCHAR2(100)  NOT NULL,
 *  AUD_CLIENT_IP VARCHAR(15)    NOT NULL,
 *  AUD_SERVER_IP VARCHAR(15)    NOT NULL,
 *  AUD_RESOURCE  VARCHAR2(1024) NOT NULL,
 *  AUD_ACTION    VARCHAR2(100)  NOT NULL,
 *  APPLIC_CD     VARCHAR2(5)    NOT NULL,
 *  AUD_DATE      TIMESTAMP      NOT NULL,
 *  AUD_GEOLOCATION   VARCHAR2(100)  NOT NULL,
 *  AUD_USERAGENT     VARCHAR2(100)  NOT NULL,
 *  AUD_LOCALE        VARCHAR2(50)   NOT NULL,
 *  AUD_HEADERS       JSON   NOT NULL,
 *  AUD_EXTRA_INFO    JSON   NOT NULL
 * )
 * </pre>
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 1.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class JdbcAuditTrailManager extends AbstractAuditTrailManager {

    /**
     * Auditable columns in the database table.
     */
    @RequiredArgsConstructor
    @Getter
    public enum AuditTableColumns {
        /**
         * User column.
         */
        USER("AUD_USER"),
        /**
         * Client IP column.
         */
        CLIENT_IP("AUD_CLIENT_IP"),
        /**
         * Server IP column.
         */
        SERVER_IP("AUD_SERVER_IP"),
        /**
         * Resource column.
         */
        RESOURCE("AUD_RESOURCE"),
        /**
         * Action column.
         */
        ACTION("AUD_ACTION"),
        /**
         * Application code column.
         */
        APPLIC_CD("APPLIC_CD"),
        /**
         * Audit date column.
         */
        DATE("AUD_DATE"),
        /**
         * Geolocation column.
         */
        GEOLOCATION("AUD_GEOLOCATION"),
        /**
         * UserAgent column.
         */
        USERAGENT("AUD_USERAGENT"),
        /**
         * Locale column.
         */
        LOCALE("AUD_LOCALE"),
        /**
         * Headers column.
         */
        HEADERS("AUD_HEADERS"),
        /**
         * Extra info column.
         */
        EXTRA_INFO("AUD_EXTRA_INFO");

        private final String columnName;
    }

    private static final String INSERT_SQL_TEMPLATE = "INSERT INTO %s ("
        + Arrays.stream(AuditTableColumns.values()).map(AuditTableColumns::getColumnName).collect(Collectors.joining(","))
        + ") VALUES ("
        + Arrays.stream(AuditTableColumns.values())
        .map(AuditTableColumns::getColumnName)
        .map(name -> StringUtils.prependIfMissing(name, ":"))
        .collect(Collectors.joining(","))
        + ')';

    private static final String DELETE_SQL_TEMPLATE = "DELETE FROM %s %s";

    private static final int DEFAULT_COLUMN_LENGTH = 512;

    /**
     * Instance of TransactionTemplate to manually execute a transaction since
     * threads are not in the same transaction.
     */
    @NotNull
    private final TransactionOperations transactionTemplate;
    @NotNull
    private final JdbcTemplate jdbcTemplate;

    @NotNull
    @Size(min = 1)
    private String tableName = "COM_AUDIT_TRAIL";

    private int columnLength = DEFAULT_COLUMN_LENGTH;

    private String selectByDateSqlTemplate = "SELECT * FROM %s WHERE %s ORDER BY " + AuditTableColumns.DATE.getColumnName() + " DESC";

    private String dateFormatterPattern = "yyyy-MM-dd 00:00:00.000000";

    private String dateFormatterFunction;

    private WhereClauseMatchCriteria cleanupCriteria = new NoMatchWhereClauseMatchCriteria();
    
    private List<String> headerNames = new ArrayList<>();
    
    @Override
    protected void saveAuditRecord(final AuditActionContext auditActionContext) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus __) {
                val principal = auditActionContext.getPrincipal();
                val userId = columnLength <= 0 || principal.length() <= columnLength
                    ? principal
                    : principal.substring(0, columnLength);
                val resourceOperatedUpon = auditActionContext.getResourceOperatedUpon();
                val resource = columnLength <= 0 || resourceOperatedUpon.length() <= columnLength
                    ? resourceOperatedUpon
                    : resourceOperatedUpon.substring(0, columnLength);
                val actionPerformed = auditActionContext.getActionPerformed();
                val action = columnLength <= 0 || actionPerformed.length() <= columnLength
                    ? actionPerformed
                    : actionPerformed.substring(0, columnLength);

                val sql = String.format(INSERT_SQL_TEMPLATE, tableName);
                val clientInfo = auditActionContext.getClientInfo();
                val locale = Optional.ofNullable(clientInfo.getLocale())
                    .map(Locale::toLanguageTag)
                    .orElseGet(Locale.US::toLanguageTag);

                val parameterMap = new HashMap<String, Object>();
                parameterMap.put(AuditTableColumns.USER.getColumnName(), userId);
                parameterMap.put(AuditTableColumns.CLIENT_IP.getColumnName(), clientInfo.getClientIpAddress());
                parameterMap.put(AuditTableColumns.SERVER_IP.getColumnName(), clientInfo.getServerIpAddress());
                parameterMap.put(AuditTableColumns.RESOURCE.getColumnName(), resource);
                parameterMap.put(AuditTableColumns.APPLIC_CD.getColumnName(), auditActionContext.getApplicationCode());
                parameterMap.put(AuditTableColumns.DATE.getColumnName(), auditActionContext.getWhenActionWasPerformed());
                parameterMap.put(AuditTableColumns.GEOLOCATION.getColumnName(), clientInfo.getGeoLocation());
                parameterMap.put(AuditTableColumns.USERAGENT.getColumnName(), clientInfo.getUserAgent());
                parameterMap.put(AuditTableColumns.LOCALE.getColumnName(), locale);
                parameterMap.put(AuditTableColumns.ACTION.getColumnName(), action);

                val converter = new MapToJsonAttributeConverter();
                parameterMap.put(AuditTableColumns.HEADERS.getColumnName(), converter.convertToDatabaseColumn(clientInfo.getHeaders()));
                parameterMap.put(AuditTableColumns.EXTRA_INFO.getColumnName(), converter.convertToDatabaseColumn(clientInfo.getExtraInfo()));

                val namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
                namedTemplate.update(sql, parameterMap);
            }
        });
    }

    @Override
    public void clean() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus __) {
                val sql = String.format(DELETE_SQL_TEMPLATE, tableName, cleanupCriteria);
                val params = cleanupCriteria.getParameterValues();
                LOGGER.info("Cleaning audit records with query [{}]", sql);
                LOGGER.debug("Query parameters: " + params);
                val count = jdbcTemplate.update(sql, params.toArray());
                LOGGER.info("[{}] records deleted.", count);
            }
        });
    }

    @Override
    public void removeAll() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus __) {
                val sql = String.format(DELETE_SQL_TEMPLATE, tableName, "aud");
                val count = jdbcTemplate.update(sql);
                LOGGER.info("[{}] records deleted.", count);
            }
        });
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        var builder = new StringBuilder("1=1 ");
        if (whereClause.containsKey(WhereClauseFields.DATE)) {
            val formatter = DateTimeFormatter.ofPattern(dateFormatterPattern, Locale.ENGLISH);
            var sinceDate = (LocalDate) whereClause.get(WhereClauseFields.DATE);
            var formattedDate = sinceDate.format(formatter);
            if (dateFormatterFunction != null) {
                val patternToUse = StringUtils.isNotBlank(dateFormatterPattern) ? dateFormatterPattern : "yyyy-MM-dd";
                formattedDate = String.format(dateFormatterFunction, sinceDate.format(formatter), patternToUse);
                builder.append(String.format("AND AUD_DATE>=%s ", formattedDate));
            } else {
                builder.append(String.format("AND AUD_DATE>='%s' ", formattedDate));
            }
        }
        if (whereClause.containsKey(WhereClauseFields.PRINCIPAL)) {
            var principal = whereClause.get(WhereClauseFields.PRINCIPAL).toString();
            builder.append(String.format("AND AUD_USER='%s' ", principal));
        }
        return getAuditRecordsSince(builder);
    }

    private Set<? extends AuditActionContext> getAuditRecordsSince(final StringBuilder where) {
        return transactionTemplate.execute((TransactionCallback<Set>) transactionStatus -> {
            val sql = String.format(selectByDateSqlTemplate, tableName, where);
            val results = new LinkedHashSet<>();
            jdbcTemplate.query(sql, resultSet -> {
                results.add(getAuditActionContext(resultSet));
            });
            return results;
        });
    }

    protected AuditActionContext getAuditActionContext(final ResultSet resultSet) throws SQLException {
        val principal = resultSet.getString(AuditTableColumns.USER.getColumnName());
        val resource = resultSet.getString(AuditTableColumns.RESOURCE.getColumnName());
        val clientIp = resultSet.getString(AuditTableColumns.CLIENT_IP.getColumnName());
        val serverIp = resultSet.getString(AuditTableColumns.SERVER_IP.getColumnName());
        val audDate = resultSet.getDate(AuditTableColumns.DATE.getColumnName());
        val appCode = resultSet.getString(AuditTableColumns.APPLIC_CD.getColumnName());
        val action = resultSet.getString(AuditTableColumns.ACTION.getColumnName());
        val userAgent = resultSet.getString(AuditTableColumns.USERAGENT.getColumnName());
        val geoLocation = resultSet.getString(AuditTableColumns.GEOLOCATION.getColumnName());
        val locale = StringUtils.defaultIfBlank(resultSet.getString(AuditTableColumns.LOCALE.getColumnName()), Locale.US.toLanguageTag());

        val headers = new HashMap<String, String>();
        for (val headerName : headerNames) {
            val headerValue = resultSet.getString(headerName);
            if (StringUtils.isNotBlank(headerValue)) {
                headers.put(headerName, headerValue);
            }
        }
        val clientInfo = new ClientInfo(clientIp, serverIp, userAgent, geoLocation)
            .setLocale(Locale.forLanguageTag(locale))
            .setHeaders(headers);
        val auditDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(audDate.getTime()), ZoneOffset.UTC);
        return new AuditActionContext(principal, resource, action, appCode, auditDate, clientInfo);
    }
}
