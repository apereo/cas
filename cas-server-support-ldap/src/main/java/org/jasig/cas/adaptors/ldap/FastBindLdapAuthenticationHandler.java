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


import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.LdapUtils;


import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

/**
 * Implementation of an LDAP handler to do a "fast bind." A fast bind skips the
 * normal two step binding process to determine validity by providing before
 * hand the path to the uid.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public class FastBindLdapAuthenticationHandler extends AbstractLdapUsernamePasswordAuthenticationHandler {

    protected final boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials credentials) throws AuthenticationException {
        DirContext dirContext = null;
        try {
            final String transformedUsername = getPrincipalNameTransformer().transform(credentials.getUsername());
            final String bindDn = LdapUtils.getFilterWithValues(getFilter(), transformedUsername);
            
            log.debug("Performing LDAP bind with credential: " + bindDn);
            dirContext = this.getContextSource().getContext(bindDn, getPasswordEncoder().encode(credentials.getPassword()));
            
            if (!StringUtils.isBlank(getSearchBase())) {
                final NamingEnumeration<SearchResult> result = dirContext.search(getSearchBase(), bindDn, getSearchControls());
                if (result.hasMore()) {
                    setAuthenticatedDistinguishedNameSearchResult(result.next());
                }
            }
            return true;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);            
            throw new LdapAuthenticationException(e.getMessage());
        } finally {
            if (dirContext != null) {
                LdapUtils.closeContext(dirContext);
            }
        }
    }
}
