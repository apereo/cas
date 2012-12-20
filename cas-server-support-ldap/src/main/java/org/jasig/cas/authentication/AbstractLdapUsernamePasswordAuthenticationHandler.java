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
package org.jasig.cas.authentication;

import java.lang.reflect.Constructor;
import java.security.GeneralSecurityException;
import java.util.List;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.support.LdapErrorDefinition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

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
    
    /** List of error definitions and their types, based on which the user will be directed to a given view in the flow **/
    private List<LdapErrorDefinition> ldapErrorDefinitions;
    
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
    
    public void setLdapErrorDefinitions(final List<LdapErrorDefinition> ldapErrorDefs) {
        this.ldapErrorDefinitions = ldapErrorDefs;
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
    
    /**
     * Maps a Spring LDAP authentication exception onto a {@link GeneralSecurityException} as required by
     * the contract of
     * {@link org.jasig.cas.authentication.AuthenticationHandler#authenticate(org.jasig.cas.authentication.Credential)}.
     * The set of configured {@link #ldapErrorDefinitions} are consulted to map exceptions based on error
     * message text into the corresponding subclass of {@link GeneralSecurityException}.
     *
     * @param authEx Authentication exception thrown by Spring LDAP components.
     *
     * @return A security exception that may be the original or one translated according to
     * configured {@link #ldapErrorDefinitions}.
     */
    protected GeneralSecurityException handleLdapError(final AuthenticationException authEx) {
        if (this.ldapErrorDefinitions == null || this.ldapErrorDefinitions.size() == 0) {
            log.debug("No error definitions are defined. Throwing default FailedLoginException.");
            return new FailedLoginException();
        }

        log.debug("Attempting to handle {}: {}", authEx.getClass().getSimpleName(), authEx.getMessage());
        for (final LdapErrorDefinition ldapErrorDef : this.ldapErrorDefinitions)
            if (ldapErrorDef.matches(authEx.getMessage())) {
                log.debug("{} matches {}", authEx, ldapErrorDef);
                try {
                    final Constructor<?> cons = ldapErrorDef.getExceptionClass().getConstructor(null);
                    return (GeneralSecurityException) cons.newInstance(null);
                } catch (Exception e) {
                    log.debug("Failed instantiating {}:{}", ldapErrorDef.getExceptionClass(), e);
                    return new GeneralSecurityException();
                }
            }

        log.debug("No matching error definition found. Throwing default FailedLoginException.");
        return new FailedLoginException();
    }
}
