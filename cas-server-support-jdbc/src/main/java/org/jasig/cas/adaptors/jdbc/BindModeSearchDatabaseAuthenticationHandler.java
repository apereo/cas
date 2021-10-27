package org.jasig.cas.adaptors.jdbc;

import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class attempts to authenticate the user by opening a connection to the
 * database with the provided username and password. Servers are provided as a
 * Properties class with the key being the URL and the property being the type
 * of database driver needed.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 *
 * @since 3.0.0
 */
@Component("bindModeSearchDatabaseAuthenticationHandler")
public class BindModeSearchDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        if (getDataSource() == null) {
            throw new GeneralSecurityException("Authentication handler is not configured correctly");
        }

        Connection connection = null;
        try {
            final String username = credential.getUsername();
            final String password = getPasswordEncoder().encode(credential.getPassword());
            connection = this.getDataSource().getConnection(username, password);
            return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
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

    @Autowired(required = false)
    @Override
    public void setDataSource(@Qualifier("bindSearchDatabaseDataSource") final DataSource dataSource) {
        super.setDataSource(dataSource);
    }
}
