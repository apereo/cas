/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

import org.jasig.cas.util.LdapUtils;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Jan Van der Velpen
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision:$ $Date:$
 * @since 3.1
 */
public final class CredentialsToLDAPAttributePrincipalResolver extends AbstractLdapPersonDirectoryCredentialsToPrincipalResolver {

    /**
     * The CredentialsToPrincipalResolver that resolves the principal from the
     * request
     */
    @NotNull
    private CredentialsToPrincipalResolver credentialsToPrincipalResolver;
    
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
        final String searchFilter = LdapUtils.getFilterWithValues(getFilter(),
            lookupAttributeValue);

        if (log.isDebugEnabled()) {
            log.debug("LDAP search with filter \"" + searchFilter + "\"");
        }
        
        try {
            // searching the directory
            final String idAttribute = getAttributeIds()[0];
            final List principalList = getLdapTemplate().search(
                getSearchBase(), searchFilter, getSearchControls(),
                
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

        } catch (final Exception e) {
            log.error(e.getMessage(), e);
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
}
