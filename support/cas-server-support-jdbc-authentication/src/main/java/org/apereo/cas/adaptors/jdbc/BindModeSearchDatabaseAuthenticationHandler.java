package org.apereo.cas.adaptors.jdbc;

import com.zaxxer.hikari.util.DriverDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


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
public class BindModeSearchDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    private final AbstractJpaProperties connectionProps;

    public BindModeSearchDatabaseAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                                       final Integer order, final DataSource dataSource, final AbstractJpaProperties connectionProps) {
        super(name, servicesManager, principalFactory, order, dataSource);
        this.connectionProps = connectionProps;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException, PreventedException {

        if (this.connectionProps.getUrl() == null) {
            throw new GeneralSecurityException("Authentication handler is not configured correctly");
        }

        DriverDataSource dataSource = null;
        Connection connection = null;
        try {
            final Properties properties = new Properties();
            final String username = credential.getUsername();
            final String password = credential.getPassword();
            String driverClass = null;

            if (StringUtils.isNotBlank(this.connectionProps.getDriverClass())) {
                driverClass = this.connectionProps.getDriverClass();
            }
            // Load the driver if not already loaded
            Class.forName(driverClass);
            dataSource = new DriverDataSource(this.connectionProps.getUrl(), driverClass, properties, username, password);
            connection = dataSource.getConnection();

            return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
        } catch (final SQLException e) {
            throw new FailedLoginException(e.getMessage());
        } catch (final Exception e) {
            throw new PreventedException("Unexpected SQL connection error", e);
        } finally {
            if (connection != null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }
}
