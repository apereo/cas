package org.apereo.cas.heimdall.authorizer.resource.policy;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.util.function.FunctionUtils;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import javax.sql.DataSource;
import java.io.Serial;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

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

    @ExpressionLanguageCapable
    private String url;
    @ExpressionLanguageCapable
    private String username;
    @ExpressionLanguageCapable
    private String password;

    private String query;

    @JsonIgnore
    private transient NamedParameterJdbcTemplate jdbcTemplate;
    
    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) throws Throwable {
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
        return FunctionUtils.doUnchecked(() -> new NamedParameterJdbcTemplate(buildDataSource()));
    }

    private static MapSqlParameterSource buildMapSqlParameterSource(final AuthorizationRequest request) {
        val parameters = new MapSqlParameterSource();
        parameters.addValue("method", request.getMethod());
        parameters.addValue("uri", request.getUri());
        parameters.addValue("namespace", request.getNamespace());
        parameters.addValue("principal", request.getPrincipal().getId());
        parameters.addValues(request.getContext());
        parameters.addValues(request.getPrincipal().getAttributes());
        return parameters;
    }

    protected DataSource buildDataSource() throws SQLException {
        val driver = DriverManager.getDriver(url);
        val driverClass = driver.getClass().getName();
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        return JpaBeans.newDataSource(driverClass, resolver.resolve(username),
            resolver.resolve(password), resolver.resolve(url));
    }
}
