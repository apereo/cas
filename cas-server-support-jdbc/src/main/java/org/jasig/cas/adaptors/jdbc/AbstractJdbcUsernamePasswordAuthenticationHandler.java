/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.jdbc;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

/**
 * Abstract class for database authentication handlers.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public abstract class AbstractJdbcUsernamePasswordAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    @NotNull
    private SimpleJdbcTemplate jdbcTemplate;
    
    @NotNull
    private DataSource dataSource;

    /**
     * Method to set the datasource and generate a JdbcTemplate.
     * 
     * @param dataSource the datasource to use.
     */
    public final void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }

    /**
     * Method to return the jdbcTemplate
     * 
     * @return a fully created JdbcTemplate.
     */
    protected final SimpleJdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }
    
    protected final DataSource getDataSource() {
        return this.dataSource;
    }
}
