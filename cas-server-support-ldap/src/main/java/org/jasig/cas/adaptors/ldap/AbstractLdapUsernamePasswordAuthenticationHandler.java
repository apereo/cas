/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.ldap;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.jasig.cas.adaptors.ldap.util.LdapErrorDefinition;
import org.jasig.cas.authentication.LdapAuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.util.Assert;

/**
 * Abstract class to handle common LDAP functionality.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public abstract class AbstractLdapUsernamePasswordAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler implements
InitializingBean {

    /** Instance of ContextSource */
    @NotNull
    private ContextSource      contextSource;

    /** The filter path to the uid of the user. */
    @NotNull
    private String             filter;

    /** Whether the LdapTemplate should ignore partial results. */
    private boolean            ignorePartialResultException = false;

    /** List of error definitions and their types, based on which the user will be directed to a given view in the flow **/
    private List<LdapErrorDefinition> ldapErrorDefinitions;

    /** LdapTemplate to execute ldap queries. */
    @NotNull
    private LdapTemplate       ldapTemplate;

    public final void afterPropertiesSet() throws Exception {
        Assert.isTrue(this.filter.contains("%u") || this.filter.contains("%U"), "filter must contain %u or %U");

        if (this.ldapTemplate == null)
            this.ldapTemplate = new LdapTemplate(this.contextSource);

        this.ldapTemplate.setIgnorePartialResultException(this.ignorePartialResultException);
        afterPropertiesSetInternal();
    }

    /**
     * Method to set the data source and generate a JdbcTemplate.
     *
     * @param contextSource the datasource to use.
     */
    public final void setContextSource(final ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    /**
     * @param filter The filter to set.
     */
    public final void setFilter(final String filter) {
        this.filter = filter;
    }

    public final void setIgnorePartialResultException(final boolean ignorePartialResultException) {
        this.ignorePartialResultException = ignorePartialResultException;
    }

    public void setLdapErrorDefinitions(final List<LdapErrorDefinition> ldapErrorDefs) {
        this.ldapErrorDefinitions = ldapErrorDefs;
    }

    protected void afterPropertiesSetInternal() throws Exception {
        // template method with nothing to do for sub classes.
    }

    protected final ContextSource getContextSource() {
        return this.contextSource;
    }

    protected final String getFilter() {
        return this.filter;
    }

    /**
     * Method to return the LdapTemplate
     *
     * @return a fully created LdapTemplate.
     */
    protected final LdapTemplate getLdapTemplate() {
        return this.ldapTemplate;
    }

    protected AuthenticationException handleLdapError(final Exception e) {
        if (this.ldapErrorDefinitions == null || this.ldapErrorDefinitions.size() == 0) {
            if (this.log.isDebugEnabled())
                this.log.debug("No error definitions are defined. Throwing error " + e.getMessage());
            return BadCredentialsAuthenticationException.ERROR;
        }

        if (this.log.isDebugEnabled())
            this.log.debug("Handling error: " + e.getMessage());

        for (final LdapErrorDefinition ldapErrorDef : this.ldapErrorDefinitions)
            if (ldapErrorDef.matches(e.getMessage())) {
                if (this.log.isDebugEnabled())
                    this.log.debug("Found error type " + ldapErrorDef.getType() +  ". Throwing error for " + e.getMessage());

                return new LdapAuthenticationException(BadCredentialsAuthenticationException.CODE, e.getMessage(), ldapErrorDef.getType());

            }

        if (this.log.isDebugEnabled())
            this.log.debug("No error definition could be matched against the error. Throwing default error for " + e.getMessage());

        return BadCredentialsAuthenticationException.ERROR;
    }

    /**
     * Available ONLY for subclasses that are doing special things with the ContextSource.
     *
     * @param ldapTemplate the LDAPTemplate to use.
     */
    protected final void setLdapTemplate(final LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }
}
