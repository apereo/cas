package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.services.ServicesManager;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;

/**
 * A JDBC querying handler that will pull back the password and
 * the private salt value for a user and validate the encoded
 * password using the public salt value. Assumes everything
 * is inside the same database table. Supports settings for
 * number of iterations as well as private salt.
 * <p>
 * If the hashing behavior and/or configuration
 * of private and public salts does not meet your needs, a extension can be developed
 * to specify alternative methods of encoding and digestion of the encoded password.
 * </p>
 *
 * @author Misagh Moayyed
 * @author Charles Hasegawa
 * @since 4.1.0
 */
@Monitorable
public class QueryAndEncodeDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    private final QueryEncodeJdbcAuthenticationProperties properties;
    private final DatabasePasswordEncoder databasePasswordEncoder;

    public QueryAndEncodeDatabaseAuthenticationHandler(final QueryEncodeJdbcAuthenticationProperties properties,
                                                       final ServicesManager servicesManager,
                                                       final PrincipalFactory principalFactory,
                                                       final DataSource dataSource,
                                                       final DatabasePasswordEncoder databasePasswordEncoder) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder(), dataSource);
        this.properties = properties;
        this.databasePasswordEncoder = databasePasswordEncoder;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential transformedCredential, final String originalPassword) throws Throwable {
        val username = transformedCredential.getUsername();
        try {
            val values = performSqlQuery(username);
            val digestedPassword = databasePasswordEncoder.encode(transformedCredential.toPassword(), values);

            if (!values.get(properties.getPasswordFieldName()).equals(digestedPassword)) {
                throw new FailedLoginException("Password does not match value on record.");
            }
            if (StringUtils.isNotBlank(properties.getExpiredFieldName()) && values.containsKey(properties.getExpiredFieldName())) {
                val dbExpired = values.get(properties.getExpiredFieldName()).toString();
                if (BooleanUtils.toBoolean(dbExpired) || "1".equals(dbExpired)) {
                    throw new AccountPasswordMustChangeException("Password has expired");
                }
            }
            if (StringUtils.isNotBlank(properties.getDisabledFieldName()) && values.containsKey(properties.getDisabledFieldName())) {
                val dbDisabled = values.get(properties.getDisabledFieldName()).toString();
                if (BooleanUtils.toBoolean(dbDisabled) || "1".equals(dbDisabled)) {
                    throw new AccountDisabledException("Account has been disabled");
                }
            }
            val principal = principalFactory.createPrincipal(username);
            return createHandlerResult(transformedCredential, principal, new ArrayList<>(0));
        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            }
            throw new FailedLoginException("Multiple records found for " + username);
        } catch (final DataAccessException e) {
            throw new PreventedException(e);
        }
    }

    protected Map<String, Object> performSqlQuery(final String username) {
        return getJdbcTemplate().queryForMap(properties.getSql(), username);
    }
}
