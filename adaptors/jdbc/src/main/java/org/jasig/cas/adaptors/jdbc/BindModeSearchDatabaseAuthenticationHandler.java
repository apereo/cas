/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.jasig.cas.authentication.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.support.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.JdbcTemplateAndDataSourceHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * This class attempts to authenticate the user by opening a connection to the database with the provided username and password. Servers are provided
 * as a Properties class with the key being the URL and the property being the type of database driver needed.
 * 
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Id$
 */
public class BindModeSearchDatabaseAuthenticationHandler extends AbstractAuthenticationHandler {
    
    private JdbcTemplateAndDataSourceHolder jdbcTemplateAndDataSourceHolder;
    
    public BindModeSearchDatabaseAuthenticationHandler(JdbcTemplateAndDataSourceHolder jdbcTemplateAndDataSourceHolder) {
        this.jdbcTemplateAndDataSourceHolder = jdbcTemplateAndDataSourceHolder;
    }
    
    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.AuthenticationRequest)
     */
    protected boolean authenticateInternal(final Credentials request) throws UnsupportedCredentialsException {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials)request;
        final String username = uRequest.getUserName();
        final String password = uRequest.getPassword();

        try {
            Connection c = this.jdbcTemplateAndDataSourceHolder.getDataSource().getConnection(username, password);
            DataSourceUtils.closeConnectionIfNecessary(c, this.jdbcTemplateAndDataSourceHolder.getDataSource());
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#supports(org.jasig.cas.authentication.principal.Credentials)
     */
    protected boolean supports(Credentials credentials) {
        return credentials != null && UsernamePasswordCredentials.class.isAssignableFrom(credentials.getClass());
    }
    
}