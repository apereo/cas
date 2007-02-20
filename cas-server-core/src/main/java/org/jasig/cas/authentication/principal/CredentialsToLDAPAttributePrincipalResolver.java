/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.util.LdapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.AttributesMapper;
import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.support.LdapContextSource;
import org.springframework.util.Assert;

/**
 * @author Jan Van der Velpen
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class CredentialsToLDAPAttributePrincipalResolver implements
    CredentialsToPrincipalResolver, InitializingBean {

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1000;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** The list of valid scope values. */
    private static final int[] VALID_SCOPE_VALUES = new int[] {
        SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE};

    /** Log instance. */
    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * The CredentialsToPrincipalResolver that resolves the principal from the
     * request
     */
    private CredentialsToPrincipalResolver credentialsToPrincipalResolver;

    /** LdapTemplate to execute ldap queries. */
    private LdapTemplate ldapTemplate;

    /** The filter path to the lookup value of the user. */
    private String filter;

    /** The attribute that contains the value that should become the principal */
    protected String principalAttributeName;

    /** The search base to find the user under. */
    private String searchBase;

    /** The scope. */
    private int scope = SearchControls.SUBTREE_SCOPE;

    /** The maximum number of results to return. */
    private int maxNumberResults = DEFAULT_MAX_NUMBER_OF_RESULTS;

    /** The amount of time to wait. */
    private int timeout = DEFAULT_TIMEOUT;

    private String[] attributeIds;

    private SearchControls getSearchControls() {
        final SearchControls constraints = new SearchControls();
        if (log.isDebugEnabled()) {
            log.debug("returning searchcontrols: scope=" + this.scope
                + "; return only attrID=" + this.attributeIds[0] + "; timeout="
                + this.timeout + "; maxNumberResults=" + this.maxNumberResults);
        }
        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(this.attributeIds);
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(this.maxNumberResults);
        return constraints;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.ldapTemplate, "ldapTemplate cannot be null");
        Assert.notNull(this.credentialsToPrincipalResolver,
            "credentialsToPrincipalResolver cannot be null");
        Assert.notNull(this.filter, "filter cannot be null");
        Assert.notNull(this.principalAttributeName,
            "principalAttributeName cannot be null");

        for (int i = 0; i < VALID_SCOPE_VALUES.length; i++) {
            if (this.scope == VALID_SCOPE_VALUES[i]) {
                return;
            }
        }
        throw new IllegalStateException("You must set a valid scope.");
    }

    public Principal resolvePrincipal(final Credentials credentials) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to resolve a principal...");
        }

        final Principal principal = this.credentialsToPrincipalResolver
            .resolvePrincipal(credentials);

        if (principal == null) {
            log
                .info("Initial principal could not be resolved from request, returning null");
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Initial principal resolved from request as \""
                + principal.getId() + "\". Trying LDAP resolve now...");
        }

        final Principal ldapPrincipal = resolveFromLDAP(principal.getId());

        if (ldapPrincipal == null) {
            log.info("Initial principal \"" + principal.getId()
                + "\" was not found in LDAP, returning null");
        }

        log.debug("Initial principal \"" + principal.getId()
            + "\" was resolved from LDAP as \"" + ldapPrincipal.getId() + "\"");

        return ldapPrincipal;
    }

    private Principal resolveFromLDAP(final String lookupAttributeValue) {
        final String searchFilter = LdapUtils.getFilterWithValues(this.filter,
            lookupAttributeValue);

        if (log.isDebugEnabled()) {
            log.debug("LDAP: starting search for value=\""
                + lookupAttributeValue + "\"" + "with searchFilter \""
                + searchFilter + "\"");
        }
        try {
            // searching the directory
            final List<String> principalList = this.ldapTemplate.search(
                this.searchBase, searchFilter, getSearchControls(),
                new AttributesMapper(){

                    public Object mapFromAttributes(final Attributes attrs)
                        throws NamingException {
                        if (log.isDebugEnabled()) {
                            log
                                .debug("LDAP: trying to map attribute \""
                                    + CredentialsToLDAPAttributePrincipalResolver.this.principalAttributeName
                                    + "\" from result.");
                        }
                        Attribute attribute = attrs
                            .get(CredentialsToLDAPAttributePrincipalResolver.this.principalAttributeName);

                        if (attribute == null) {
                            log.debug("LDAP: attribute was null in result.");
                            return null;
                        }

                        if (log.isDebugEnabled()) {
                            log
                                .debug("LDAP: found attribute in result, value=\""
                                    + attribute.get() + "\"");
                        }

                        return attribute.get();
                    }
                });

            if (principalList.isEmpty()) {
                log.warn("No result was found for: " + lookupAttributeValue);
                return null;
            }

            if (principalList.size() > 1) {
                log.error("LDAP: search returned multiple results"
                    + " for value=\"" + lookupAttributeValue + "\""
                    + " which is not allowed.");

                return null;
            }

            return new SimplePrincipal(principalList.get(0));

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
    public final void setPrincipalAttributeName(String principalAttributeName) {
        this.principalAttributeName = principalAttributeName;
        this.attributeIds = new String[] {principalAttributeName};
    }

    /**
     * @param filter The scope to set.
     */
    public final void setScope(final int scope) {
        this.scope = scope;
    }

    /**
     * @param maxNumberResults The maxNumberResults to set.
     */
    public final void setMaxNumberResults(final int maxNumberResults) {
        this.maxNumberResults = maxNumberResults;
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
