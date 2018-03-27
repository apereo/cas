package org.apereo.cas.adaptors.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
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
public class BindModeSearchDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    public BindModeSearchDatabaseAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                                       final Integer order, final DataSource dataSource) {
        super(name, servicesManager, principalFactory, order, dataSource);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException, PreventedException {

        if (getDataSource() == null) {
            throw new GeneralSecurityException("Authentication handler is not configured correctly");
        }

        Connection connection = null;
        try {
            final String username = credential.getUsername();
            final String password = credential.getPassword();
            connection = this.getDataSource().getConnection(username, password);
            return createHandlerResult(credential, this.principalFactory.createPrincipal(username), new ArrayList<>(0));
        } catch (final SQLException e) {
            throw new FailedLoginException(e.getMessage());
        } catch (final Exception e) {
            throw new PreventedException("Unexpected SQL connection error", e);
        } finally {
            if (connection != null) {
                DataSourceUtils.releaseConnection(connection, this.getDataSource());
            }
        }
    }
}
