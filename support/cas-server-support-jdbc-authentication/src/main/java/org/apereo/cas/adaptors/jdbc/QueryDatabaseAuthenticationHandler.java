package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryJdbcAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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

    private final QueryJdbcAuthenticationProperties properties;

    private final Map<String, Object> principalAttributeMap;

    public QueryDatabaseAuthenticationHandler(final QueryJdbcAuthenticationProperties properties,
                                              final ServicesManager servicesManager,
                                              final PrincipalFactory principalFactory,
                                              final DataSource dataSource,
                                              final Map<String, Object> attributes) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder(), dataSource);
        this.properties = properties;
        this.principalAttributeMap = attributes;

        if (StringUtils.isBlank(properties.getFieldPassword())) {
            LOGGER.warn("When the password field is left undefined, CAS will skip comparing database and user passwords for equality "
                + ", (specially if the query results do not contain the password field),"
                + "and will instead only rely on a successful query execution with returned results in order to verify credentials");
        }
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException, PreventedException {
        val attributes = Maps.<String, List<Object>>newHashMapWithExpectedSize(this.principalAttributeMap.size());
        val username = credential.getUsername();
        val password = credential.getPassword();
        try {
            val dbFields = query(credential);
            if (dbFields.containsKey(properties.getFieldPassword())) {
                val dbPassword = (String) dbFields.get(properties.getFieldPassword());

                val originalPasswordMatchFails = StringUtils.isNotBlank(originalPassword) && !matches(originalPassword, dbPassword);
                val originalPasswordEquals = StringUtils.isBlank(originalPassword) && !StringUtils.equals(password, dbPassword);
                if (originalPasswordMatchFails || originalPasswordEquals) {
                    throw new FailedLoginException("Password does not match value on record.");
                }
            } else {
                LOGGER.debug("Password field is not found in the query results. Checking for result count...");
                if (!dbFields.containsKey("total")) {
                    throw new FailedLoginException("Missing field 'total' from the query results for " + username);
                }

                val count = dbFields.get("total");
                if (count == null || !NumberUtils.isCreatable(count.toString())) {
                    throw new FailedLoginException("Missing field value 'total' from the query results for "
                        + username + " or value not parseable as a number");
                }

                val number = NumberUtils.createNumber(count.toString());
                if (number.longValue() != 1) {
                    throw new FailedLoginException("No records found for user " + username);
                }
            }

            if (StringUtils.isNotBlank(properties.getFieldDisabled()) && dbFields.containsKey(properties.getFieldDisabled())) {
                val dbDisabled = dbFields.get(properties.getFieldDisabled()).toString();
                if (BooleanUtils.toBoolean(dbDisabled) || "1".equals(dbDisabled)) {
                    throw new AccountDisabledException("Account has been disabled");
                }
            }
            if (StringUtils.isNotBlank(properties.getFieldExpired()) && dbFields.containsKey(properties.getFieldExpired())) {
                val dbExpired = dbFields.get(properties.getFieldExpired()).toString();
                if (BooleanUtils.toBoolean(dbExpired) || "1".equals(dbExpired)) {
                    throw new AccountPasswordMustChangeException("Password has expired");
                }
            }
            collectPrincipalAttributes(attributes, dbFields);
        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            }
            throw new FailedLoginException("Multiple records found for " + username);
        } catch (final DataAccessException e) {
            throw new PreventedException(e);
        }
        val principal = this.principalFactory.createPrincipal(username, attributes);
        return createHandlerResult(credential, principal, new ArrayList<>(0));
    }

    private Map<String, Object> query(final UsernamePasswordCredential credential) {
        if (properties.getSql().contains("?")) {
            return getJdbcTemplate().queryForMap(properties.getSql(), credential.getUsername());
        }
        val parameters = new LinkedHashMap<String, Object>();
        parameters.put("username", credential.getUsername());
        parameters.put("password", credential.getPassword());
        return getNamedParameterJdbcTemplate().queryForMap(properties.getSql(), parameters);
    }

    private void collectPrincipalAttributes(final Map<String, List<Object>> attributes, final Map<String, Object> dbFields) {
        this.principalAttributeMap.forEach((key, names) -> {
            val attribute = dbFields.get(key);
            if (attribute != null) {
                LOGGER.debug("Found attribute [{}] from the query results", key);
                val attributeNames = (Collection<String>) names;
                attributeNames.forEach(s -> {
                    LOGGER.debug("Principal attribute [{}] is virtually remapped/renamed to [{}]", key, s);
                    attributes.put(s, CollectionUtils.wrap(attribute.toString()));
                });
            } else {
                LOGGER.warn("Requested attribute [{}] could not be found in the query results", key);
            }
        });
    }
}
