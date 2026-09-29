package org.apereo.cas.heimdall.authorizer.resource.policy;

import module java.base;
import module java.sql;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.CloseableDataSource;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * This is {@link JdbcAuthorizationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class JdbcAuthorizationPolicy implements ResourceAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = 5242641641967628938L;

    private static final String DATA_SOURCE_BEAN_PREFIX = "heimdallJdbcDataSource-";

    private static final ReentrantLock DATA_SOURCE_LOCK = new ReentrantLock();

    @ExpressionLanguageCapable
    private String url;

    @ExpressionLanguageCapable
    private String username;

    @ExpressionLanguageCapable
    @ToString.Exclude
    private String password;

    private String query;

    private String dataSourceName;

    private String queryTimeout = "PT5S";

    @JsonIgnore
    private transient NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) {
        this.jdbcTemplate = Objects.requireNonNullElseGet(this.jdbcTemplate, this::buildJdbcTemplate);
        val parameters = buildMapSqlParameterSource(request);
        return jdbcTemplate.query(query, parameters, rs -> {
            val result = rs.next() && rs.getBoolean("authorized");
            return AuthorizationResult.from(result);
        });
    }

    /**
     * Build jdbc template.
     *
     * @return the named parameter jdbc template
     */
    public NamedParameterJdbcTemplate buildJdbcTemplate() {
        return FunctionUtils.doUnchecked(() -> {
            val template = new NamedParameterJdbcTemplate(resolveDataSource());
            template.getJdbcTemplate().setQueryTimeout(resolveQueryTimeoutSeconds());
            return template;
        });
    }

    /**
     * Resolve the query timeout in seconds, rounding a positive sub-second timeout up to one second.
     * A blank, zero, negative or infinite timeout falls back to the driver default.
     *
     * @return the query timeout in seconds
     */
    protected int resolveQueryTimeoutSeconds() {
        if (Beans.isNeverDurable(queryTimeout) || Beans.isInfinitelyDurable(queryTimeout)) {
            return 0;
        }
        val timeout = Beans.newDuration(queryTimeout);
        return timeout.isPositive() ? Math.clamp(timeout.toSeconds(), 1, Integer.MAX_VALUE) : 0;
    }

    /**
     * Resolve the data source by bean name from the application context, registering a new connection
     * pool under that name when no such bean exists. The pool then outlives policy reloads, is shared by
     * policies with the same connection settings, and is closed with the application context. The bean name
     * is {@code dataSourceName} when defined, or derived from the resolved URL and username otherwise.
     *
     * @return the data source
     * @throws Exception if the data source cannot be created
     */
    protected DataSource resolveDataSource() throws Exception {
        val applicationContext = resolveApplicationContext();
        if (applicationContext == null || !applicationContext.isActive()) {
            return buildDataSource();
        }
        val beanName = resolveDataSourceBeanName();
        DATA_SOURCE_LOCK.lock();
        try {
            if (!applicationContext.containsBean(beanName)) {
                val dataSource = buildDataSource();
                LOGGER.debug("Registering Heimdall JDBC data source [{}]", beanName);
                applicationContext.registerBean(beanName, CloseableDataSource.class, () -> dataSource,
                    definition -> definition.setAutowireCandidate(false));
            }
            return applicationContext.getBean(beanName, DataSource.class);
        } finally {
            DATA_SOURCE_LOCK.unlock();
        }
    }

    protected @Nullable GenericApplicationContext resolveApplicationContext() {
        return ApplicationContextProvider.getConfigurableApplicationContext() instanceof final GenericApplicationContext context
            ? context
            : null;
    }

    protected String resolveDataSourceBeanName() {
        if (StringUtils.isNotBlank(dataSourceName)) {
            return dataSourceName;
        }
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        return DATA_SOURCE_BEAN_PREFIX + DigestUtils.sha256(resolver.resolve(url) + '|' + resolver.resolve(username));
    }

    private static MapSqlParameterSource buildMapSqlParameterSource(final AuthorizationRequest request) {
        val parameters = new MapSqlParameterSource();
        parameters.addValues(request.getContext());
        parameters.addValues(request.getPrincipal().getAttributes());
        parameters.addValue("method", request.getMethod());
        parameters.addValue("uri", request.getUri());
        parameters.addValue("namespace", request.getNamespace());
        parameters.addValue("principal", request.getPrincipal().getId());
        if (request.isAuthZen()) {
            parameters.addValue("subjectType", request.getSubject().getType());
            parameters.addValue("subjectId", request.getSubject().getId());
            parameters.addValue("resourceType", request.getResource().getType());
            parameters.addValue("resourceId", request.getResource().getId());
            parameters.addValue("action", request.getAction().getName());
        }
        return parameters;
    }

    protected CloseableDataSource buildDataSource() throws SQLException {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val resolvedUrl = resolver.resolve(url);
        val driverClass = DriverManager.getDriver(resolvedUrl).getClass().getName();
        return JpaBeans.newPoolingDataSource(driverClass, resolver.resolve(username),
            resolver.resolve(password), resolvedUrl);
    }
}
