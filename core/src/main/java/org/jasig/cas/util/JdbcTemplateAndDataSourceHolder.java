/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Helper class that exposes JdbcTemplate and the corresponding DataSource.
 * Useful in cases where multiple inheritance is not possible, so the delagation is being used instead.
 * 
 * @author Dmitriy Kopylenko
 * @version $Id$
 */
public final class JdbcTemplateAndDataSourceHolder extends JdbcDaoSupport {

}