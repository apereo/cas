package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.jdbc.authn.SearchJdbcAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DataAccessException;

import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
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
public class SearchModeSearchDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {
    private final SearchJdbcAuthenticationProperties properties;

    public SearchModeSearchDatabaseAuthenticationHandler(final SearchJdbcAuthenticationProperties properties,
                                                         final ServicesManager servicesManager,
                                                         final PrincipalFactory principalFactory,
                                                         final DataSource datasource) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder(), datasource);
        this.properties = properties;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException, PreventedException {
        val sql = "SELECT COUNT('x') FROM ".concat(properties.getTableUsers()).concat(" WHERE ").concat(properties.getFieldUser())
            .concat(" = ? AND ").concat(properties.getFieldPassword()).concat("= ?");
        val username = credential.getUsername();
        try {
            LOGGER.debug("Executing SQL query [{}]", sql);
            val count = getJdbcTemplate().queryForObject(sql, Integer.class, username, credential.getPassword());
            if (count == null || count == 0) {
                throw new FailedLoginException(username + " not found with SQL query.");
            }
            return createHandlerResult(credential, this.principalFactory.createPrincipal(username), new ArrayList<>(0));
        } catch (final DataAccessException e) {
            throw new PreventedException(e);
        }
    }
}
