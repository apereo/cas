/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.apereo.cas.adaptors.jdbc;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.Principal;
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
import java.util.List;
import java.util.Map;

import org.sakaiproject.user.impl.PasswordService;

/**
 * Class that if provided a query that returns a password (parameter of query
 * must be username) will compare by using Sakai PasswordService.
 * 
 * @author Shoji Kajita
 * @since 3.4
 */
@Slf4j
public class QuerySakaiDatabaseAuthenticationHandler extends QueryDatabaseAuthenticationHandler {

    private final String sql;
    private final String fieldPassword;
    private final String fieldExpired;
    private final String fieldDisabled;
    private final Map<String, Object> principalAttributeMap;
    private final PasswordService pwdService;

    public QuerySakaiDatabaseAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                              final PrincipalFactory principalFactory,
                                              final Integer order, final DataSource dataSource, final String sql,
                                              final String fieldPassword, final String fieldExpired, final String fieldDisabled,
                                              final Map<String, Object> attributes) {

        super(name, servicesManager, principalFactory, order, dataSource, sql, fieldPassword, fieldExpired, fieldDisabled, attributes);
        this.sql = sql;
        this.fieldPassword = fieldPassword;
        this.fieldExpired = fieldExpired;
        this.fieldDisabled = fieldDisabled;
        this.principalAttributeMap = attributes;
        this.pwdService = new PasswordService();
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential, final String originalPassword)
        throws GeneralSecurityException, PreventedException {
        if (StringUtils.isBlank(this.sql) || getJdbcTemplate() == null) {
            throw new GeneralSecurityException("Authentication handler is not configured correctly. "
                + "No SQL statement or JDBC template is found.");
        }

        val attributes = Maps.<String, List<Object>>newHashMapWithExpectedSize(this.principalAttributeMap.size());
        val username = credential.getUsername();
        val password = credential.getPassword();
        try {
            val dbFields = query(credential);
            if (dbFields.containsKey(this.fieldPassword)) {
                val dbPassword = (String) dbFields.get(this.fieldPassword);

                if (!pwdService.check(password, dbPassword)) {
                    throw new FailedLoginException("Password does not match value on record.");
                }
            }

            if (StringUtils.isNotBlank(this.fieldDisabled) && dbFields.containsKey(this.fieldDisabled)) {
                val dbDisabled = dbFields.get(this.fieldDisabled).toString();
                if (BooleanUtils.toBoolean(dbDisabled) || "1".equals(dbDisabled)) {
                    throw new AccountDisabledException("Account has been disabled");
                }
            }
            if (StringUtils.isNotBlank(this.fieldExpired) && dbFields.containsKey(this.fieldExpired)) {
                val dbExpired = dbFields.get(this.fieldExpired).toString();
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
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }
        val principal = this.principalFactory.createPrincipal(username, attributes);
        return createHandlerResult(credential, principal, new ArrayList<>(0));
    }

    private Map<String, Object> query(final UsernamePasswordCredential credential) {
        if (this.sql.contains("?")) {
            return getJdbcTemplate().queryForMap(this.sql, credential.getUsername());
        }
        val parameters = new LinkedHashMap<String, Object>();
        parameters.put("username", credential.getUsername());
        parameters.put("password", credential.getPassword());
        return getNamedJdbcTemplate().queryForMap(this.sql, parameters);
    }

    private void collectPrincipalAttributes(final Map<String, List<Object>> attributes, final Map<String, Object> dbFields) {
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
    }
}
