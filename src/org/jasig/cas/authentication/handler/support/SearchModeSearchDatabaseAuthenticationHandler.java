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
 * Class that given a table, username field and password field will query a database table with the provided encryption technique to see if the user
 * exists. This class provides a failover. If provided multiple datasources, on a DataAccessResourceFailureException the next datasource in the list
 * will be tried. This class defaults to a PasswordTranslator of PlainTextPasswordTranslator.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */

// TODO: is this efficient enough??
public class SearchModeSearchDatabaseAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final String SQL_PREFIX = "Select count(*) from ";

    private List dataSources;

    private String fieldUser;

    private String fieldPassword;

    private String tableUsers;

    private PasswordTranslator passwordTranslator;

    private String SQL;

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public boolean authenticate(final Credentials request) {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials)request;

        for (Iterator iter = this.dataSources.iterator(); iter.hasNext();) {

            try {
                final DataSource dataSource = (DataSource)iter.next();
                final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                final String encyptedPassword = this.passwordTranslator.translate(uRequest.getPassword());
                int count = jdbcTemplate.queryForInt(this.SQL, new Object[] {uRequest.getUserName(), encyptedPassword});

                if (count > 0)
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
        if (this.dataSources == null || this.fieldPassword == null || this.fieldUser == null || this.tableUsers == null) {
            throw new IllegalStateException("passwordTranslator, dataSources, fieldPassword, fieldUser and tableUsers must be set on "
                + this.getClass().getName());
        }
        
        if (this.passwordTranslator == null) {
            this.passwordTranslator = new PlainTextPasswordTranslator();
            log.info("PasswordTranslator not set.  Using default PasswordTranslator of class " + this.passwordTranslator.getClass().getName());
        }

        this.SQL = SQL_PREFIX + this.tableUsers + " Where " + this.fieldUser + " = ? And " + this.fieldPassword + " = ?";
    }

    /**
     * @param dataSources The dataSources to set.
     */
    public void setDataSources(final List dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * @param fieldPassword The fieldPassword to set.
     */
    public void setFieldPassword(final String fieldPassword) {
        this.fieldPassword = fieldPassword;
    }

    /**
     * @param fieldUser The fieldUser to set.
     */
    public void setFieldUser(final String fieldUser) {
        this.fieldUser = fieldUser;
    }

    /**
     * @param passwordTranslator The passwordTranslator to set.
     */
    public void setPasswordTranslator(final PasswordTranslator passwordTranslator) {
        this.passwordTranslator = passwordTranslator;
    }

    /**
     * @param tableUsers The tableUsers to set.
     */
    public void setTableUsers(final String tableUsers) {
        this.tableUsers = tableUsers;
    }
}
