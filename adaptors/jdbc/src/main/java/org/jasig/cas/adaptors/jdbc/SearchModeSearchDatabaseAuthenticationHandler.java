/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.PasswordTranslator;
import org.jasig.cas.util.support.PlainTextPasswordTranslator;

/**
 * Class that given a table, username field and password field will query a database table with the provided encryption technique to see if the user
 * exists. This class defaults to a PasswordTranslator of PlainTextPasswordTranslator.
 * 
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Id$
 */

public class SearchModeSearchDatabaseAuthenticationHandler extends
    AbstractJdbcAuthenticationHandler {

    protected final Log log = LogFactory.getLog(getClass());

    private static final String SQL_PREFIX = "Select count('x') from ";

    private String fieldUser;

    private String fieldPassword;

    private String tableUsers;

    private PasswordTranslator passwordTranslator;

    private String sql;

    protected boolean authenticateInternal(final Credentials request)
        throws AuthenticationException {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials)request;
        final String encyptedPassword = this.passwordTranslator
            .translate(uRequest.getPassword());

        final int count = getJdbcTemplate().queryForInt(this.sql,
            new Object[] {uRequest.getUserName(), encyptedPassword});

        return count > 0;
    }

    protected boolean supports(Credentials credentials) {
        return credentials != null
            && UsernamePasswordCredentials.class.isAssignableFrom(credentials
                .getClass());
    }

    public void init() throws Exception {
        if (this.fieldPassword == null || this.fieldUser == null
            || this.tableUsers == null) {
            throw new IllegalStateException(
                "fieldPassword, fieldUser and tableUsers must be set on "
                    + this.getClass().getName());
        }

        if (this.passwordTranslator == null) {
            this.passwordTranslator = new PlainTextPasswordTranslator();
            log
                .info("PasswordTranslator not set.  Using default PasswordTranslator of class "
                    + this.passwordTranslator.getClass().getName());
        }

        this.sql = SQL_PREFIX + this.tableUsers + " Where " + this.fieldUser
            + " = ? And " + this.fieldPassword + " = ?";
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
    public void setPasswordTranslator(
        final PasswordTranslator passwordTranslator) {
        this.passwordTranslator = passwordTranslator;
    }

    /**
     * @param tableUsers The tableUsers to set.
     */
    public void setTableUsers(final String tableUsers) {
        this.tableUsers = tableUsers;
    }
}