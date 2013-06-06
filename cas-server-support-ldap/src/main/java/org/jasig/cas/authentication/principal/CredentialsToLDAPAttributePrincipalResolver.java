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

import org.jasig.cas.authentication.Credential;
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
 * @since 3.1
 */
public final class CredentialsToLDAPAttributePrincipalResolver extends AbstractLdapPersonDirectoryCredentialsToPrincipalResolver {

    /**
     * The CredentialsToPrincipalResolver that resolves the principal from the
     * request.
     */
    @NotNull
    private CredentialsToPrincipalResolver credentialsToPrincipalResolver;

    @Override
    protected String extractPrincipalId(final Credential credential) {
        final Principal principal = this.credentialsToPrincipalResolver
                .resolvePrincipal(credential);

        if (principal == null) {
            log.info("Initial principal could not be resolved from request, "
                    + "returning null");
            return null;
        }

        log.debug("Resolved {}. Trying LDAP resolve now...", principal);

        final String ldapPrincipal = resolveFromLDAP(principal.getId());

        if (ldapPrincipal == null) {
            log.info("Initial principal {} was not found in LDAP, returning null", principal.getId());
        } else {
            log.debug("Resolved {} to {}", principal, ldapPrincipal);
        }

        return ldapPrincipal;
    }

    private String resolveFromLDAP(final String lookupAttributeValue) {
        final String searchFilter = LdapUtils.getFilterWithValues(getFilter(),
                lookupAttributeValue);

        log.debug("LDAP search with filter {}", searchFilter);

        try {
            // searching the directory
            final String idAttribute = getAttributeIds()[0];
            final List principalList = getLdapTemplate().search(
                    getSearchBase(), searchFilter, getSearchControls(),

                    new AttributesMapper() {
                        @Override
                        public Object mapFromAttributes(final Attributes attrs)
                                throws NamingException {
                            final Attribute attribute = attrs.get(idAttribute);
                            if (attribute == null) {
                                log.debug("Principal attribute {} not found in LDAP search results. Returning null.",
                                        idAttribute);
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
    @Override
    public boolean supports(final Credential credential) {
        return this.credentialsToPrincipalResolver.supports(credential);
    }

    /**
     * @param credentialsToPrincipalResolver The credentialsToPrincipalResolver
     * to set.
     */
    public final void setCredentialsToPrincipalResolver(
            final CredentialsToPrincipalResolver credentialsToPrincipalResolver) {
        this.credentialsToPrincipalResolver = credentialsToPrincipalResolver;
    }
}
