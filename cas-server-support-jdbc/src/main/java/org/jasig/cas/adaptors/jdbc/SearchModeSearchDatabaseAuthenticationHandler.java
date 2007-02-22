/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.jdbc;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.util.Assert;

/**
 * Class that given a table, username field and password field will query a
 * database table with the provided encryption technique to see if the user
 * exists. This class defaults to a PasswordTranslator of
 * PlainTextPasswordTranslator.
 * 
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 */

public class SearchModeSearchDatabaseAuthenticationHandler extends
    AbstractJdbcUsernamePasswordAuthenticationHandler {

    private static final String SQL_PREFIX = "Select count('x') from ";

    private String fieldUser;

    private String fieldPassword;

    private String tableUsers;

    private String sql;

    protected final boolean authenticateUsernamePasswordInternal(
        UsernamePasswordCredentials credentials) throws AuthenticationException {
        final String encyptedPassword = getPasswordEncoder().encode(
            credentials.getPassword());

        final int count = getJdbcTemplate().queryForInt(this.sql,
            credentials.getUsername(), encyptedPassword);

        return count > 0;
    }

    protected final void initDao() throws Exception {
        Assert.notNull(this.fieldPassword, "fieldPassword cannot be null");
        Assert.notNull(this.fieldUser, "fieldUser cannot be null");
        Assert.notNull(this.tableUsers, "tableUsers cannot be null");

        this.sql = SQL_PREFIX + this.tableUsers + " Where " + this.fieldUser
            + " = ? And " + this.fieldPassword + " = ?";
    }

    /**
     * @param fieldPassword The fieldPassword to set.
     */
    public final void setFieldPassword(final String fieldPassword) {
        this.fieldPassword = fieldPassword;
    }

    /**
     * @param fieldUser The fieldUser to set.
     */
    public final void setFieldUser(final String fieldUser) {
        this.fieldUser = fieldUser;
    }

    /**
     * @param tableUsers The tableUsers to set.
     */
    public final void setTableUsers(final String tableUsers) {
        this.tableUsers = tableUsers;
    }
}