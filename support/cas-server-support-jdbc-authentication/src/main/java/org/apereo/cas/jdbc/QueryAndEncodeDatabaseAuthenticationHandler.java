package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.monitor.Monitorable;
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
public class QueryAndEncodeDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler<QueryEncodeJdbcAuthenticationProperties> {

    private final DatabasePasswordEncoder databasePasswordEncoder;

    public QueryAndEncodeDatabaseAuthenticationHandler(final QueryEncodeJdbcAuthenticationProperties properties,

                                                       final PrincipalFactory principalFactory,
                                                       final DataSource dataSource,
                                                       final DatabasePasswordEncoder databasePasswordEncoder) {
        super(properties, principalFactory, dataSource);
        this.databasePasswordEncoder = databasePasswordEncoder;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential transformedCredential, final String originalPassword) throws Throwable {
        val username = transformedCredential.getUsername();
        try {
            val sqlQueryResults = performSqlQuery(username);
            val digestedPassword = databasePasswordEncoder.encode(transformedCredential.toPassword(), sqlQueryResults);

            if (!sqlQueryResults.get(properties.getPasswordFieldName()).equals(digestedPassword)) {
                throw new FailedLoginException("Password does not match value on record.");
            }
            if (StringUtils.isNotBlank(properties.getExpiredFieldName()) && sqlQueryResults.containsKey(properties.getExpiredFieldName())) {
                val dbExpired = sqlQueryResults.get(properties.getExpiredFieldName()).toString();
                if (BooleanUtils.toBoolean(dbExpired) || "1".equals(dbExpired)) {
                    throw new AccountPasswordMustChangeException("Password has expired");
                }
            }
            if (StringUtils.isNotBlank(properties.getDisabledFieldName()) && sqlQueryResults.containsKey(properties.getDisabledFieldName())) {
                val dbDisabled = sqlQueryResults.get(properties.getDisabledFieldName()).toString();
                if (BooleanUtils.toBoolean(dbDisabled) || "1".equals(dbDisabled)) {
                    throw new AccountDisabledException("Account has been disabled");
                }
            }
            val attributes = collectPrincipalAttributes(sqlQueryResults);
            val principal = principalFactory.createPrincipal(username, attributes);
            return createHandlerResult(transformedCredential, principal, new ArrayList<>());
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
