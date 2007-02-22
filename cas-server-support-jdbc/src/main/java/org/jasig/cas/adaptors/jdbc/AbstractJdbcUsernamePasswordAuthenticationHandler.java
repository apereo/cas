/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.jdbc;

import javax.sql.DataSource;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.util.Assert;

/**
 * Abstract class for database authentication handlers.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public abstract class AbstractJdbcUsernamePasswordAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    private SimpleJdbcTemplate jdbcTemplate;
    
    private DataSource dataSource;

    /**
     * Method to set the datasource and generate a JdbcTemplate.
     * 
     * @param dataSource the datasource to use.
     */
    public final void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
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

    protected final void afterPropertiesSetInternal() throws Exception {
        Assert.notNull(this.jdbcTemplate, "jdbcTemplate cannot be null (DataSource is required)");
        initDao();
    }

    /**
     * Template method to do additional set up in the dao implementations.
     * 
     * @throws Exception if there is a problem during set up.
     */
    protected void initDao() throws Exception {
        // nothing to do here...stub
    }

}
