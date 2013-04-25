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

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.ldaptive.Credential;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LDAP authentication handler that uses the ldaptive <code>Authenticator</code> component underneath.
 * This handler provides simple attribute resolution machinery by reading attributes from the entry
 * corresponding to the DN of the bound user (in the bound security context) upon successful authentication.
 * Principal resolution is controlled by the following properties:
 *
 * <ul>
 *     <ol>{@link #setPrincipalIdAttribute(String)}</ol>
 *     <ol>{@link #setPrincipalAttributeMap(java.util.Map)}</ol>
 * </ul>
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class LdapAuthenticationHandler implements AuthenticationHandler {

    /** Logger instance. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Performs LDAP authentication given username/password. */
    @NotNull
    private final Authenticator authenticator;

    /** Component name. */
    @NotNull
    private String name = LdapAuthenticationHandler.class.getSimpleName();

    /** Name of attribute to be used for resolved principal. */
    private String principalIdAttribute;

    /** Mapping of LDAP attribute name to principal attribute name. */
    @NotNull
    private Map<String, String> principalAttributeMap = Collections.emptyMap();

    /** Set of LDAP attributes fetch from an entry as part of the authentication process. */
    private String[] authenticatedEntryAttributes;


    /**
     * Creates a new authentication handler that delegates to the given authenticator.
     *
     * @param  authenticator  Ldaptive authenticator component.
     */
    public LdapAuthenticationHandler(final Authenticator authenticator) {
        this.authenticator = authenticator;
    }


    /**
     * Sets the component name. Defaults to simple class name.
     *
     * @param  name  Authentication handler name.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the name of the LDAP principal attribute whose value should be used for the
     * principal ID.
     *
     * @param attributeName LDAP attribute name.
     */
    public void setPrincipalIdAttribute(final String attributeName) {
        this.principalIdAttribute = attributeName;
    }

    /**
     * Sets the mapping of additional principal attributes where the key is the LDAP attribute
     * name and the value is the principal attribute name. The key set defines the set of
     * attributes read from the LDAP entry at authentication time. Note that the principal ID attribute
     * should not be listed among these attributes.
     *
     * @param attributeNameMap Map of LDAP attribute name to principal attribute name.
     */
    public void setPrincipalAttributeMap(final Map<String, String> attributeNameMap) {
        this.principalAttributeMap = attributeNameMap;
    }

    /**
     * Initializes the component after properties are set.
     */
    @PostConstruct
    public void initialize() {
        final List<String> attributes = new ArrayList<String>();
        if (this.principalIdAttribute != null) {
            attributes.add(this.principalIdAttribute);
        }
        attributes.addAll(this.principalAttributeMap.keySet());
        this.authenticatedEntryAttributes = new String[attributes.size()];
        this.authenticatedEntryAttributes = attributes.toArray(this.authenticatedEntryAttributes);
    }

    @Override
    public HandlerResult authenticate(final Credentials credential)
            throws GeneralSecurityException, PreventedException {
        final AuthenticationResponse response;
        final UsernamePasswordCredentials upc = (UsernamePasswordCredentials) credential;
        try {
            log.debug("Attempting LDAP authentication for {}", credential);
            final AuthenticationRequest request = new AuthenticationRequest(
                    upc.getUsername(),
                    new Credential(upc.getPassword()),
                    this.authenticatedEntryAttributes);
            response = this.authenticator.authenticate(request);
        } catch (LdapException e) {
            throw new PreventedException("Unexpected LDAP error", e);
        }
        log.debug("LDAP response: {}", response);
        if (response.getResult()) {
            return new HandlerResult(this, createPrincipal(response.getLdapEntry()));
        }
        final AccountState state = response.getAccountState();
        if (state != null && state.getError() != null) {
            state.getError().throwSecurityException();
        }
        throw new FailedLoginException("LDAP authentication failed.");
    }

    @Override
    public boolean supports(final Credentials credential) {
        return credential instanceof UsernamePasswordCredentials;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Creates a CAS principal with attributes if the LDAP entry contains principal attributes.
     *
     * @param ldapEntry LDAP entry that may contain principal attributes.
     * @return Principal if the LDAP entry contains at least a principal ID attribute value, null otherwise.
     */
    private Principal createPrincipal(final LdapEntry ldapEntry) {
        final LdapAttribute principalAttr = ldapEntry.getAttribute(this.principalIdAttribute);
        if (principalAttr == null || principalAttr.size() == 0) {
            return null;
        }
        if (principalAttr.size() > 1) {
            log.warn("Found multiple values for principal ID attribute: {}.  Using first value.", principalAttr);
        }
        final Map<String, Object> attributeMap = new LinkedHashMap<String, Object>(this.principalAttributeMap.size());
        for (String ldapAttrName : this.principalAttributeMap.keySet()) {
            final LdapAttribute attr = ldapEntry.getAttribute(ldapAttrName);
            if (attr != null) {
                log.debug("Found principal attribute: {}", attr);
                final String principalAttrName = this.principalAttributeMap.get(ldapAttrName);
                if (attr.size() > 1) {
                    attributeMap.put(principalAttrName, attr.getStringValues());
                } else {
                    attributeMap.put(principalAttrName, attr.getStringValue());
                }
            }
        }
        return new SimplePrincipal(principalAttr.getStringValue(), attributeMap);
    }
}
