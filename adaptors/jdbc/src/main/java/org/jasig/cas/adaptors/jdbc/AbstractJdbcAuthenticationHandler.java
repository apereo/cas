/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.jdbc;

import org.jasig.cas.authentication.handler.support.AbstractAuthenticationHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractJdbcAuthenticationHandler extends
		AbstractAuthenticationHandler implements InitializingBean {
	
	private JdbcTemplate jdbcTemplate;

	/**
	 * @return Returns the jdbcTemplate.
	 */
	public final JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	/**
	 * @param jdbcTemplate The jdbcTemplate to set.
	 */
	public final void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public final void afterPropertiesSet() throws Exception {
		if (this.jdbcTemplate == null) {
			throw new IllegalStateException("jdbcTemplate must be set on " + this.getClass().getName());
		}
		
		initHandler();
	}
	
	protected void initHandler() throws Exception {
		// designed to be over-ridden if needed
	}
}
