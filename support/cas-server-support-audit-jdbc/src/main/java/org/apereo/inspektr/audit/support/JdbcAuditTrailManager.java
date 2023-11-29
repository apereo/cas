package org.apereo.inspektr.audit.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.util.StringUtils;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 *  AUD_GEOLOCATION   VARCHAR2(100)   NOT NULL,
 *  AUD_USERAGENT     VARCHAR2(100)   NOT NULL
 * )
 * </pre>
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class JdbcAuditTrailManager extends NamedParameterJdbcDaoSupport implements AuditTrailManager, DisposableBean {

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
        USERAGENT("AUD_USERAGENT");

        private final String columnName;
    }

    private static final String INSERT_SQL_TEMPLATE = "INSERT INTO %s ("
        + Arrays.stream(AuditTableColumns.values())
        .map(AuditTableColumns::getColumnName)
        .collect(Collectors.joining(",")) + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String DELETE_SQL_TEMPLATE = "DELETE FROM %s %s";

    private static final int DEFAULT_COLUMN_LENGTH = 512;

    /**
     * Instance of TransactionTemplate to manually execute a transaction since
     * threads are not in the same transaction.
     */
    @NotNull
    private final TransactionOperations transactionTemplate;

    @NotNull
    @Size(min = 1)
    @Setter
    private String tableName = "COM_AUDIT_TRAIL";

    @Setter
    private int columnLength = DEFAULT_COLUMN_LENGTH;

    @Setter
    private String selectByDateSqlTemplate = "SELECT * FROM %s WHERE %s ORDER BY " + AuditTableColumns.DATE.getColumnName() + " DESC";

    @Setter
    private String dateFormatterPattern = "yyyy-MM-dd 00:00:00.000000";

    @Setter
    private String dateFormatterFunction;

    @NotNull
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private boolean defaultExecutorService = true;

    @Setter
    private boolean asynchronous = true;

    /**
     * Criteria used to determine records that should be deleted on cleanup.
     */
    @Setter
    private WhereClauseMatchCriteria cleanupCriteria = new NoMatchWhereClauseMatchCriteria();

    @Setter
    private List<String> headerNames = new ArrayList<>();


    @Override
    public void record(final AuditActionContext auditActionContext) {
        val command = new LoggingTask(auditActionContext,
            this.transactionTemplate, this.columnLength);
        if (this.asynchronous) {
            this.executorService.execute(command);
        } else {
            command.run();
        }
    }

    /**
     * Sets executor service.
     *
     * @param executorService the executor service
     */
    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
        this.defaultExecutorService = false;
    }

    /**
     * We only shut down the default executor service.  We assume, that if you've injected one, its being managed elsewhere.
     */
    @Override
    public void destroy() {
        if (this.defaultExecutorService) {
            this.executorService.shutdown();
        }
    }

    @Override
    public void clean() {
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {
                val sql = String.format(DELETE_SQL_TEMPLATE, tableName, cleanupCriteria);
                val params = cleanupCriteria.getParameterValues();
                LOGGER.info("Cleaning audit records with query [{}]", sql);
                LOGGER.debug("Query parameters: " + params);
                val count = getJdbcTemplate().update(sql, params.toArray());
                LOGGER.info("[{}] records deleted.", count);
            }
        });
    }

    @Override
    public void removeAll() {
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {
                val sql = String.format(DELETE_SQL_TEMPLATE, tableName, org.apache.commons.lang3.StringUtils.EMPTY);
                val count = getJdbcTemplate().update(sql);
                LOGGER.info("[{}] records deleted.", count);
            }
        });
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {

        var builder = new StringBuilder("1=1 ");
        if (whereClause.containsKey(WhereClauseFields.DATE)) {
            val formatter = DateTimeFormatter.ofPattern(this.dateFormatterPattern, Locale.ENGLISH);
            var sinceDate = (LocalDate) whereClause.get(WhereClauseFields.DATE);
            var formattedDate = sinceDate.format(formatter);
            if (this.dateFormatterFunction != null) {
                var patternToUse = StringUtils.hasLength(this.dateFormatterPattern)
                    ? this.dateFormatterPattern
                    : "yyyy-MM-dd";
                formattedDate = String.format(this.dateFormatterFunction, sinceDate.format(formatter), patternToUse);
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

    @RequiredArgsConstructor
    private final class LoggingTask implements Runnable {

        private final AuditActionContext auditActionContext;

        private final TransactionOperations transactionTemplate;

        private final int columnLength;

        @Override
        public void run() {
            this.transactionTemplate
                .execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {
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

                        val clientInfo = auditActionContext.getClientInfo();
                        getJdbcTemplate().update(
                            String.format(INSERT_SQL_TEMPLATE, tableName),
                            userId,
                            clientInfo.getClientIpAddress(),
                            clientInfo.getServerIpAddress(),
                            resource,
                            action,
                            auditActionContext.getApplicationCode(),
                            auditActionContext.getWhenActionWasPerformed(),
                            clientInfo.getGeoLocation(),
                            clientInfo.getUserAgent());
                    }
                });
        }
    }

    private Set<? extends AuditActionContext> getAuditRecordsSince(final StringBuilder where) {
        return transactionTemplate.execute((TransactionCallback<Set>) transactionStatus -> {
            val sql = String.format(this.selectByDateSqlTemplate, tableName, where);
            Set<AuditActionContext> results = new LinkedHashSet<>();
            getJdbcTemplate().query(sql, resultSet -> {
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

        val headers = new HashMap<String, String>();
        for (val headerName : headerNames) {
            val headerValue = resultSet.getString(headerName);
            if (StringUtils.hasText(headerValue)) {
                headers.put(headerName, headerValue);
            }
        }
        val clientInfo = new ClientInfo(clientIp, serverIp, userAgent, geoLocation).setHeaders(headers);
        val auditDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(audDate.getTime()), ZoneOffset.UTC);
        return new AuditActionContext(principal, resource, action, appCode, auditDate, clientInfo);
    }
}
