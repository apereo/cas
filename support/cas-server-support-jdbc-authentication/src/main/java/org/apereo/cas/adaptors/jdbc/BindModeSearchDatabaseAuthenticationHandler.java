package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.util.ArrayList;

/**
 * This class attempts to authenticate the user by opening a connection to the
 * database with the provided username and password. Servers are provided as a
 * Properties class with the key being the URL and the property being the type
 * of database driver needed.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Slf4j
@Monitorable
public class BindModeSearchDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    public BindModeSearchDatabaseAuthenticationHandler(final String name, final ServicesManager servicesManager,
        final PrincipalFactory principalFactory,
        final Integer order, final DataSource dataSource) {
        super(name, servicesManager, principalFactory, order, dataSource);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential credential, final String originalPassword) throws Throwable {
        val username = credential.getUsername();
        val password = credential.toPassword();
        try (val connection = getDataSource().getConnection(username, password)) {
            LOGGER.trace("Established connection to schema [{}]", connection.getSchema());
            val principal = this.principalFactory.createPrincipal(username);
            return createHandlerResult(credential, principal, new ArrayList<>(0));
        } catch (final Throwable e) {
            throw new FailedLoginException(e.getMessage());
        }
    }
}
