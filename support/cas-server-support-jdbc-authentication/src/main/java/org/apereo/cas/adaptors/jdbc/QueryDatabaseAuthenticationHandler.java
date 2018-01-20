package org.apereo.cas.adaptors.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class that if provided a query that returns a password (parameter of query
 * must be username) will compare that password to a translated version of the
 * password provided by the user. If they match, then authentication succeeds.
 * Default password translator is plaintext translator.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Slf4j
public class QueryDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    private final String sql;
    private final String fieldPassword;
    private final String fieldExpired;
    private final String fieldDisabled;
    private final Map<String, Object> principalAttributeMap;

    public QueryDatabaseAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                              final PrincipalFactory principalFactory,
                                              final Integer order, final DataSource dataSource, final String sql,
                                              final String fieldPassword, final String fieldExpired, final String fieldDisabled,
                                              final Map<String, Object> attributes) {
        super(name, servicesManager, principalFactory, order, dataSource);
        this.sql = sql;
        this.fieldPassword = fieldPassword;
        this.fieldExpired = fieldExpired;
        this.fieldDisabled = fieldDisabled;
        this.principalAttributeMap = attributes;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException, PreventedException {
        if (StringUtils.isBlank(this.sql) || getJdbcTemplate() == null) {
            throw new GeneralSecurityException("Authentication handler is not configured correctly. "
                + "No SQL statement or JDBC template is found.");
        }

        final Map<String, Object> attributes = new LinkedHashMap<>(this.principalAttributeMap.size());
        final String username = credential.getUsername();
        final String password = credential.getPassword();
        try {
            final Map<String, Object> dbFields = getJdbcTemplate().queryForMap(this.sql, username);
            final String dbPassword = (String) dbFields.get(this.fieldPassword);

            if ((StringUtils.isNotBlank(originalPassword) && !matches(originalPassword, dbPassword))
                || (StringUtils.isBlank(originalPassword) && !StringUtils.equals(password, dbPassword))) {
                throw new FailedLoginException("Password does not match value on record.");
            }
            if (StringUtils.isNotBlank(this.fieldDisabled)) {
                final Object dbDisabled = dbFields.get(this.fieldDisabled);
                if (dbDisabled != null && (Boolean.TRUE.equals(BooleanUtils.toBoolean(dbDisabled.toString())) || dbDisabled.equals(Integer.valueOf(1)))) {
                    throw new AccountDisabledException("Account has been disabled");
                }
            }
            if (StringUtils.isNotBlank(this.fieldExpired)) {
                final Object dbExpired = dbFields.get(this.fieldExpired);
                if (dbExpired != null && (Boolean.TRUE.equals(BooleanUtils.toBoolean(dbExpired.toString())) || dbExpired.equals(1))) {
                    throw new AccountPasswordMustChangeException("Password has expired");
                }
            }
            this.principalAttributeMap.forEach((key, names) -> {
                final Object attribute = dbFields.get(key);

                if (attribute != null) {
                    LOGGER.debug("Found attribute [{}] from the query results", key);
                    final Collection<String> attributeNames = (Collection<String>) names;
                    attributeNames.forEach(s -> {
                        LOGGER.debug("Principal attribute [{}] is virtually remapped/renamed to [{}]", key, s);
                        attributes.put(s, CollectionUtils.wrap(attribute.toString()));
                    });
                } else {
                    LOGGER.warn("Requested attribute [{}] could not be found in the query results", key);
                }

            });

        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            }
            throw new FailedLoginException("Multiple records found for " + username);
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username, attributes), new ArrayList<>(0));
    }
}
