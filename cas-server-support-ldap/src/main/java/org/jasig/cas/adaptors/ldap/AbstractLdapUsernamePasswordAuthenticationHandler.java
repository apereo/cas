/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.ContextSource;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;

/**
 * Abstract class to handle common LDAP functionality.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public abstract class AbstractLdapUsernamePasswordAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler implements InitializingBean {

    /** LdapTemplate to execute ldap queries. */
    @NotNull
    private LdapTemplate ldapTemplate;
    
    /** Instance of ContextSource */
    @NotNull
    private ContextSource contextSource;

    /** The filter path to the uid of the user. */
    @NotNull
    private String filter;
    
    /** Whether the LdapTemplate should ignore partial results. */
    private boolean ignorePartialResultException = false;

    /**
     * Method to set the datasource and generate a JdbcTemplate.
     * 
     * @param contextSource the datasource to use.
     */
    public final void setContextSource(final ContextSource contextSource) {
        this.contextSource = contextSource;
    }
    
    public final void setIgnorePartialResultException(final boolean ignorePartialResultException) {
        this.ignorePartialResultException = ignorePartialResultException;
    }

    /**
     * Method to return the LdapTemplate
     * 
     * @return a fully created LdapTemplate.
     */
    protected final LdapTemplate getLdapTemplate() {
        return this.ldapTemplate;
    }

    protected final ContextSource getContextSource() {
        return this.contextSource;
    }

    protected final String getFilter() {
        return this.filter;
    }

    public final void afterPropertiesSet() throws Exception {
        Assert.isTrue(this.filter.contains("%u") || this.filter.contains("%U"), "filter must contain %u or %U");

        if (this.ldapTemplate == null) {
            this.ldapTemplate = new LdapTemplate(this.contextSource);
        }

        this.ldapTemplate.setIgnorePartialResultException(this.ignorePartialResultException);
        afterPropertiesSetInternal();
    }

    /**
     * Available ONLY for subclasses that are doing special things with the ContextSource.
     *
     * @param ldapTemplate the LDAPTemplate to use.
     */
    protected final void setLdapTemplate(final LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    protected void afterPropertiesSetInternal() throws Exception {
        // template method with nothing to do for sub classes.
    }

    /**
     * @param filter The filter to set.
     */
    public final void setFilter(final String filter) {
        this.filter = filter;
    }
}
