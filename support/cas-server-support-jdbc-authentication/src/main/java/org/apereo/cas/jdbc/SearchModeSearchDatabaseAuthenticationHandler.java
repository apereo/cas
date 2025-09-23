package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.jdbc.authn.SearchJdbcAuthenticationProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.util.ArrayList;

/**
 * Class that given a table, username field and password field will query a
 * database table with the provided encryption technique to see if the user
 * exists. This class defaults to a PasswordTranslator of
 * PlainTextPasswordTranslator.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Slf4j
@Monitorable
public class SearchModeSearchDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler<SearchJdbcAuthenticationProperties> {
    public SearchModeSearchDatabaseAuthenticationHandler(final SearchJdbcAuthenticationProperties properties,

                                                         final PrincipalFactory principalFactory,
                                                         final DataSource datasource) {
        super(properties, principalFactory, datasource);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential credential, final String originalPassword) throws Throwable {
        val sql = "SELECT COUNT('x') FROM ".concat(properties.getTableUsers())
            .concat(" WHERE ")
            .concat(properties.getFieldUser())
            .concat(" = ? AND ")
            .concat(properties.getFieldPassword()).concat("= ?");
        val username = credential.getUsername();
        try {
            LOGGER.debug("Executing SQL query [{}]", sql);
            val count = getJdbcTemplate().queryForObject(sql, Integer.class, username, credential.toPassword());
            if (count == null || count == 0) {
                throw new FailedLoginException(username + " not found with SQL query.");
            }
            val principal = principalFactory.createPrincipal(username);
            return createHandlerResult(credential, principal, new ArrayList<>());
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            throw new FailedLoginException(e.getMessage());
        }
    }
}
