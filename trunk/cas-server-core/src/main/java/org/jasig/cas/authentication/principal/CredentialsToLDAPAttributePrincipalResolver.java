/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.util.LdapUtils;
import org.jasig.cas.util.annotation.IsIn;
import org.jasig.cas.util.annotation.NotNull;

import org.springframework.ldap.AttributesMapper;
import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.support.LdapContextSource;

/**
 * @author Jan Van der Velpen
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision:$ $Date:$
 * @since 3.1
 */
public final class CredentialsToLDAPAttributePrincipalResolver extends AbstractPersonDirectoryCredentialsToPrincipalResolver {

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 2;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /**
     * The CredentialsToPrincipalResolver that resolves the principal from the
     * request
     */
    @NotNull
    private CredentialsToPrincipalResolver credentialsToPrincipalResolver;

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
    @IsIn({SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE})
    private int scope = SearchControls.SUBTREE_SCOPE;

    /** The amount of time to wait. */
    private int timeout = DEFAULT_TIMEOUT;

    private SearchControls getSearchControls() {
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
    
    protected String extractPrincipalId(final Credentials credentials) {
        final Principal principal = this.credentialsToPrincipalResolver
            .resolvePrincipal(credentials);

        if (principal == null) {
            log.info("Initial principal could not be resolved from request, "
                + "returning null");
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Resolved " + principal + ". Trying LDAP resolve now...");
        }

        final String ldapPrincipal = resolveFromLDAP(principal.getId());

        if (ldapPrincipal == null) {
            log.info("Initial principal \"" + principal.getId()
                + "\" was not found in LDAP, returning null");
        } else {
            log.debug("Resolved " + principal + " to " + ldapPrincipal);
        }
     
        return ldapPrincipal;
    }

    private String resolveFromLDAP(final String lookupAttributeValue) {
        final String searchFilter = LdapUtils.getFilterWithValues(this.filter,
            lookupAttributeValue);

        if (log.isDebugEnabled()) {
            log.debug("LDAP search with filter \"" + searchFilter + "\"");
        }
        
        try {
            // searching the directory
            final String idAttribute = this.attributeIds[0];
            final List principalList = this.ldapTemplate.search(
                this.searchBase, searchFilter, getSearchControls(),
                
                new AttributesMapper() {
                    public Object mapFromAttributes(final Attributes attrs)
                        throws NamingException {
                        final Attribute attribute = attrs.get(idAttribute);
                        if (attribute == null) {
                            log.debug("Principal attribute \"" + idAttribute + "\" "
                                + "not found in LDAP search results. Returning null.");
                            return null;
                        }
                        return attribute.get();
                    }
                    
                });
            if (principalList.isEmpty()) {
                log.debug("LDAP search returned zero results.");
                return null;
            }
            if (principalList.size() > 1) {
                log.error("LDAP search returned multiple results "
                    + "for filter \"" + searchFilter + "\", "
                    + "which is not allowed.");

                return null;
            }
            return (String) principalList.get(0);

        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    } 

    /*
     * Delegates checking to the configured CredentialsToPrincipalResolver.
     */
    public boolean supports(final Credentials credentials) {
        return this.credentialsToPrincipalResolver.supports(credentials);
    }

    /**
     * @param credentialsToPrincipalResolver The credentialsToPrincipalResolver
     * to set.
     */
    public final void setCredentialsToPrincipalResolver(
        CredentialsToPrincipalResolver credentialsToPrincipalResolver) {
        this.credentialsToPrincipalResolver = credentialsToPrincipalResolver;
    }

    /**
     * Method to set the datasource and generate a LDAPTemplate.
     * 
     * @param dataSource the datasource to use.
     */
    public final void setContextSource(final LdapContextSource contextSource) {
        this.ldapTemplate = new LdapTemplate(contextSource);
    }

    /**
     * @param filter The LDAP filter to set.
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * @param principalAttributeName The principalAttributeName to set.
     */
    public final void setPrincipalAttributeName(final String principalAttributeName) {
        this.attributeIds = new String[] {principalAttributeName};
    }

    /**
     * @param filter The scope to set.
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
}
