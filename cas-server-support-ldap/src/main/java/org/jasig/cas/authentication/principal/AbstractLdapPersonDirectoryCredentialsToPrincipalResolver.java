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
package org.jasig.cas.authentication.principal;

import java.util.Arrays;

import javax.naming.directory.SearchControls;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public abstract class AbstractLdapPersonDirectoryCredentialsToPrincipalResolver extends
    AbstractPersonDirectoryCredentialsToPrincipalResolver {

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 2;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;
    
    /** LdapTemplate to execute ldap queries. */
    @NotNull
    private LdapTemplate ldapTemplate;

    /** The filter path to the lookup value of the user. */
    @NotNull
    private String filter;

    /** The attribute that contains the value that should become the principal */
    @NotNull
    private String[] attributeIds;

    /** The search base to find the user under. */
    @NotNull
    private String searchBase;

    /** The scope. */
    @Min(0)
    @Max(2)
    private int scope = SearchControls.SUBTREE_SCOPE;

    /** The amount of time to wait. */
    private int timeout = DEFAULT_TIMEOUT;

    protected final SearchControls getSearchControls() {
        final SearchControls constraints = new SearchControls();
        if (log.isDebugEnabled()) {
            log.debug("returning searchcontrols: scope=" + this.scope
                + "; search base=" + this.searchBase
                + "; attributes=" + Arrays.toString(this.attributeIds)
                + "; timeout=" + this.timeout);
        }
        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(this.attributeIds);
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(DEFAULT_MAX_NUMBER_OF_RESULTS);
        return constraints;
    }
    
    /**
     * Method to set the datasource and generate a LDAPTemplate.
     * 
     * @param contextSource the datasource to use.
     */
    public final void setContextSource(final ContextSource contextSource) {
        this.ldapTemplate = new LdapTemplate(contextSource);

        // Fix for http://www.ja-sig.org/issues/browse/CAS-663
        this.ldapTemplate.setIgnorePartialResultException(true);
    }

    /**
     * @param filter The LDAP filter to set.
     */
    public final void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * @param principalAttributeName The principalAttributeName to set.
     */
    public final void setPrincipalAttributeName(final String principalAttributeName) {
        this.attributeIds = new String[] {principalAttributeName};
    }

    /**
     * @param scope The scope to set.
     */
    public final void setScope(final int scope) {
        this.scope = scope;
    }

    /**
     * @param searchBase The searchBase to set.
     */
    public final void setSearchBase(final String searchBase) {
        this.searchBase = searchBase;
    }

    /**
     * @param timeout The timeout to set.
     */
    public final void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    
    protected final String getFilter() {
        return this.filter;
    }

    
    protected final String[] getAttributeIds() {
        return this.attributeIds;
    }

    
    protected final String getSearchBase() {
        return this.searchBase;
    }

    
    protected final int getTimeout() {
        return this.timeout;
    }
    
    protected final LdapTemplate getLdapTemplate() {
        return this.ldapTemplate;
    }
}
