/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.PasswordTranslator;
import org.jasig.cas.util.support.PlainTextPasswordTranslator;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Class that if provided a query that returns a password (paramater of query must be username) will compare that password to a translated version of
 * the password provided by the user. If they match, then authentication succeeds. Default password translator is plaintext translator. Note that this
 * class provides failover if provided multiple datasources. On DataAccessResourceFailureException, the next datasource will be tried.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
// TODO: efficient???
public class QueryDatabaseAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private PasswordTranslator passwordTranslator = new PlainTextPasswordTranslator();

    private List dataSources;

    private String sql;

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public boolean authenticate(final Credentials request) {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials)request;
        final String username = uRequest.getUserName();
        final String password = uRequest.getPassword();
        final String encryptedPassword = this.passwordTranslator.translate(password);

        for (Iterator iter = this.dataSources.iterator(); iter.hasNext();) {
            try {
                final DataSource dataSource = (DataSource)iter.next();
                final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                final String dbPassword = (String)jdbcTemplate.queryForObject(this.sql, new Object[] {username}, String.class);

                if (dbPassword.equals(encryptedPassword))
                    return true;
            }
            catch (DataAccessResourceFailureException e) {
                // this means the server failed!!!
            }
            catch (DataAccessException dae) {
                return false;
            }
        }
        return false;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.dataSources == null || this.sql == null || this.passwordTranslator == null) {
            throw new IllegalStateException("dataSources, sql, and passwordTranslator must be set on " + this.getClass().getName());
        }
    }

    /**
     * @param dataSources The dataSources to set.
     */
    public void setDataSources(final List dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * @param passwordTranslator The passwordTranslator to set.
     */
    public void setPasswordTranslator(final PasswordTranslator passwordTranslator) {
        this.passwordTranslator = passwordTranslator;
    }

    /**
     * @param sql The sql to set.
     */
    public void setSql(final String sql) {
        this.sql = sql;
    }

}
