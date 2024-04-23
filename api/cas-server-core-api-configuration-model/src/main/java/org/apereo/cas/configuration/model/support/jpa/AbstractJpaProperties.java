package org.apereo.cas.configuration.model.support.jpa;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Common properties for all jpa configs.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-jdbc-drivers")
@Accessors(chain = true)
@JsonFilter("AbstractJpaProperties")
@SuppressWarnings("UnescapedEntity")
public abstract class AbstractJpaProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = 761486823496930920L;

    /**
     * The database dialect is a configuration setting for platform independent software (JPA, Hibernate, etc)
     * which allows such software to translate its generic SQL statements into vendor specific DDL, DML.
     */
    private String dialect = "org.hibernate.dialect.HSQLDialect";

    /**
     * Hibernate feature to automatically validate and exports DDL to the schema.
     * By default, creates and drops the schema automatically when a session is starts and ends.
     * Setting the value to {@code validate} or {@code none} may be more desirable for production,
     * but any of the following options can be used:
     * <ul>
     *     <li>{@code validate}: Validate the schema, but make no changes to the database.</li>
     *     <li>{@code update}: Update the schema.</li>
     *     <li>{@code create}: Create the schema, destroying previous data.</li>
     *     <li>{@code create-drop}: Drop the schema at the end of the session.</li>
     *     <li>{@code none}: Do nothing.</li>
     * </ul>
     * <p>
     * Note that during a version migration where any schema has changed {@code create-drop} will result
     * in the loss of all data as soon as CAS is started. For transient data like tickets this is probably
     * not an issue, but in cases like the audit table important data could be lost. Using `update`, while safe
     * for data, is confirmed to result in invalid database state. {@code validate} or {@code none} settings
     * are likely the only safe options for production use.
     * </p>
     * For more info, <a href="http://docs.spring.io/spring-framework/docs/current/javadoc-api">see this</a>.
     */
    private String ddlAuto = "update";

    /**
     * The JDBC driver used to connect to the database.
     */
    @RequiredProperty
    private String driverClass = "org.hsqldb.jdbcDriver";

    /**
     * The database connection URL.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String url = "jdbc:hsqldb:mem:cas-hsql-database";

    /**
     * The database user.
     * <p>
     * The database user must have sufficient permissions to be able to handle
     * schema changes and updates, when needed.
     */
    @RequiredProperty
    private String user = "sa";

    /**
     * The database connection password.
     */
    @RequiredProperty
    private String password = StringUtils.EMPTY;

    /**
     * Qualifies unqualified table names with the given catalog in generated SQL.
     */
    private String defaultCatalog;

    /**
     * Qualify unqualified table names with the given schema/tablespace in generated SQL.
     */
    private String defaultSchema;

    /**
     * The SQL query to be executed to test the validity of connections.
     * This is for "legacy" databases that do not support the JDBC4 {@code Connection.isValid()} API.
     */
    private String healthQuery = StringUtils.EMPTY;

    /**
     * Controls the maximum amount of time that a connection is allowed to sit idle in the pool.
     */
    @DurationCapable
    private String idleTimeout = "PT10M";

    /**
     * Indicates the maximum number of milliseconds that the service
     * can wait to obtain a connection.
     */
    @DurationCapable
    private String connectionTimeout = "PT30S";

    /**
     * Attempts to do a JNDI data source look up for the data source name specified.
     * Will attempt to locate the data source object as is.
     */
    private String dataSourceName;

    /**
     * Additional settings provided by Hibernate (or the connection provider) in form of key-value pairs.
     */
    private Map<String, String> properties = new HashMap<>(0);

    /**
     * Database connection pooling settings.
     */
    @NestedConfigurationProperty
    private ConnectionPoolingProperties pool = new ConnectionPoolingProperties();

    /**
     * Controls the amount of time that a connection can be out of the pool before a message
     * is logged indicating a possible connection leak.
     */
    @DurationCapable
    private String leakThreshold = "PT6S";

    /**
     * Allow hibernate to generate query statistics.
     */
    private boolean generateStatistics;

    /**
     * A non-zero value enables use of JDBC2 batch updates by Hibernate. e.g. recommended values between 5 and 30.
     */
    private int batchSize = 100;

    /**
     * Used to specify number of rows to be fetched in a select query.
     */
    private int fetchSize = 100;

    /**
     * Set the pool initialization failure timeout.
     * <ul>
     * <li>Any value greater than zero will be treated as a timeout for pool initialization.
     * The calling thread will be blocked from continuing until a successful connection
     * to the database, or until the timeout is reached.  If the timeout is reached, then
     * a {@code PoolInitializationException} will be thrown. </li>
     * <li>A value of zero will <i>not</i>  prevent the pool from starting in the
     * case that a connection cannot be obtained. However, upon start the pool will
     * attempt to obtain a connection and validate that the {@code connectionTestQuery}
     * and {@code connectionInitSql} are valid.  If those validations fail, an exception
     * will be thrown.  If a connection cannot be obtained, the validation is skipped
     * and the the pool will start and continue to try to obtain connections in the
     * background. This can mean that callers to {@code DataSource#getConnection()} may
     * encounter exceptions. </li>
     * <li>A value less than zero will <i>not</i> bypass any connection attempt and
     * validation during startup, and therefore the pool will start immediately.  The
     * pool will continue to try to obtain connections in the background. This can mean
     * that callers to {@code DataSource#getConnection()} may encounter exceptions. </li>
     * </ul>
     * Note that if this timeout value is greater than or equal to zero (0), and therefore an
     * initial connection validation is performed, this timeout does not override the
     * {@code connectionTimeout} or {@code validationTimeout}; they will be honored before this
     * timeout is applied.  The default value is one millisecond.
     */
    private long failFastTimeout = 1;

    /**
     * This property determines whether data source isolates internal pool queries, such as the connection alive test,
     * in their own transaction.
     * <p>
     * Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction.
     * This property only applies if {@link #autocommit} is disabled.
     */
    private boolean isolateInternalQueries;

    /**
     * The default auto-commit behavior of connections in the pool.
     * Determined whether queries such as update/insert should be immediately executed
     * without waiting for an underlying transaction.
     */
    private boolean autocommit;

    /**
     * Configures the Connections to be added to the pool as read-only Connections.
     */
    private boolean readOnly;

    /**
     * Fully-qualified name of the class that can control the physical naming strategy of hibernate.
     */
    private String physicalNamingStrategyClassName = "org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategy";

    /**
     * Defines the isolation level for transactions.
     *
     * @see org.springframework.transaction.TransactionDefinition
     */
    private String isolationLevelName = "ISOLATION_READ_COMMITTED";

    /**
     * Defines the propagation behavior for transactions.
     *
     * @see org.springframework.transaction.TransactionDefinition
     */
    private String propagationBehaviorName = "PROPAGATION_REQUIRED";
}
