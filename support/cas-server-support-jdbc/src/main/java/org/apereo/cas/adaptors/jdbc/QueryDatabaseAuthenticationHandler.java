package org.apereo.cas.adaptors.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Class that if provided a query that returns a password (parameter of query
 * must be username) will compare that password to a translated version of the
 * password provided by the user. If they match, then authentication succeeds.
 * Default password translator is plaintext translator.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 *
 * @since 3.0.0
 */
public class QueryDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    
    private String sql;

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential, final String originalPassword)
            throws GeneralSecurityException, PreventedException {

        if (StringUtils.isBlank(this.sql) || getJdbcTemplate() == null) {
            throw new GeneralSecurityException("Authentication handler is not configured correctly. "  
                + "No SQL statement or JDBC template is found.");
        }

        final String username = credential.getUsername();
        final String password = credential.getPassword();

        try {
            final String dbPassword = getJdbcTemplate().queryForObject(this.sql, String.class, username);

            if ((StringUtils.isNotBlank(originalPassword) && !this.matches(originalPassword, dbPassword))
                  || (StringUtils.isBlank(originalPassword) && !StringUtils.equals(password, dbPassword))) {
                throw new FailedLoginException("Password does not match value on record.");
            }
        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            } 
            throw new FailedLoginException("Multiple records found for " + username);
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }

        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
    }


    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
                throws GeneralSecurityException, PreventedException {
        return authenticateUsernamePasswordInternal(credential, null);
    }

    public void setSql(final String sql) {
        this.sql = sql;
    }
}
