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
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.SearchEntryResolver;
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
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Performs LDAP authentication given username/password. */
    @NotNull
    private final Authenticator authenticator;

    /** Component name. */
    @NotNull
    private String name = LdapAuthenticationHandler.class.getSimpleName();

    /** Name of attribute to be used for resolved principal. */
    private String principalIdAttribute;

    /** Flag indicating whether multiple values are allowed fo principalIdAttribute. */
    private boolean allowMultiplePrincipalAttributeValues = false;

    /** Mapping of LDAP attribute name to principal attribute name. */
    @NotNull
    protected Map<String, String> principalAttributeMap = Collections.emptyMap();

    /** Set of LDAP attributes fetch from an entry as part of the authentication process. */
    private String[] authenticatedEntryAttributes;

    /**
     * Creates a new authentication handler that delegates to the given authenticator.
     *
     * @param  authenticator  Ldaptive authenticator component.
     */
    public LdapAuthenticationHandler(@NotNull final Authenticator authenticator) {
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
     * Sets a flag that determines whether multiple values are allowed for the {@link #principalIdAttribute}.
     * This flag only has an effect if {@link #principalIdAttribute} is configured. If multiple values are detected
     * when the flag is false, the first value is used and a warning is logged. If multiple values are detected
     * when the flag is true, an exception is raised.
     *
     * @param allowed True to allow multiple principal ID attribute values, false otherwise.
     */
    public void setAllowMultiplePrincipalAttributeValues(final boolean allowed) {
        this.allowMultiplePrincipalAttributeValues = allowed;
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

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException,
                                PreventedException {
        final AuthenticationResponse response;
        final UsernamePasswordCredential upc = (UsernamePasswordCredential) credential;
        try {
            logger.debug("Attempting LDAP authentication for {}", credential);
            final AuthenticationRequest request = new AuthenticationRequest(upc.getUsername(),
                    new org.ldaptive.Credential(upc.getPassword()),
                    this.authenticatedEntryAttributes);
            // NOTE
            // revisit when ldaptive provides support for conveying authentication request attributes to
            // red entry resolver internally
            if (this.authenticator.getEntryResolver() instanceof SearchEntryResolver) {
                ((SearchEntryResolver) this.authenticator.getEntryResolver()).setReturnAttributes(
                        this.authenticatedEntryAttributes);
            }
            response = this.authenticator.authenticate(request);
        } catch (final LdapException e) {
            throw new PreventedException("Unexpected LDAP error", e);
        }
        logger.debug("LDAP response: {}", response);

        examineAccountState(response);

        if (response.getResult()) {
            return doPostAuthentication(upc, response);
        }

        if (AuthenticationResultCode.DN_RESOLUTION_FAILURE == response.getAuthenticationResultCode()) {
            throw new AccountNotFoundException(upc.getUsername() + " not found.");
        }
        throw new FailedLoginException("Invalid credentials.");
    }

    /**
     * Examine account state to see if any errors are present.
     * If so, throws the relevant security exception.
     *
     * @param response the response
     * @throws LoginException the login exception
     */
    protected void examineAccountState(final AuthenticationResponse response) throws LoginException {
        final AccountState state = response.getAccountState();
        if (state != null && state.getError() != null) {
            state.getError().throwSecurityException();
        }
    }

    /**
     * Handle post authentication processing.
     *
     * @param credential the credential
     * @param response the response
     * @return the handler result
     * @throws LoginException the login exception
     */
    protected HandlerResult doPostAuthentication(
            final UsernamePasswordCredential credential,
            final AuthenticationResponse response) throws LoginException {
        return new HandlerResult(
                this,
                new BasicCredentialMetaData(credential),
                createPrincipal(credential.getUsername(), response.getLdapEntry()));
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Creates a CAS principal with attributes if the LDAP entry contains principal attributes.
     *
     * @param username Username that was successfully authenticated which is used for principal ID when
     *                 {@link #setPrincipalIdAttribute(String)} is not specified.
     * @param ldapEntry LDAP entry that may contain principal attributes.
     *
     * @return Principal if the LDAP entry contains at least a principal ID attribute value, null otherwise.
     *
     * @throws LoginException On security policy errors related to principal creation.
     */
    protected Principal createPrincipal(final String username, final LdapEntry ldapEntry) throws LoginException {
        final String id;
        if (this.principalIdAttribute != null) {
            final LdapAttribute principalAttr = ldapEntry.getAttribute(this.principalIdAttribute);
            if (principalAttr == null || principalAttr.size() == 0) {
                throw new LoginException(this.principalIdAttribute + " attribute not found for " + username);
            }
            if (principalAttr.size() > 1) {
                if (this.allowMultiplePrincipalAttributeValues) {
                    logger.warn(
                            "Found multiple values for principal ID attribute: {}. Using first value={}.",
                            principalAttr,
                            principalAttr.getStringValue());
                } else {
                    throw new LoginException("Multiple principal values not allowed: " + principalAttr);
                }
            }
            id = principalAttr.getStringValue();
        } else {
            id = username;
        }
        final Map<String, Object> attributeMap = new LinkedHashMap<String, Object>(this.principalAttributeMap.size());
        for (String ldapAttrName : this.principalAttributeMap.keySet()) {
            final LdapAttribute attr = ldapEntry.getAttribute(ldapAttrName);
            if (attr != null) {
                logger.debug("Found principal attribute: {}", attr);
                final String principalAttrName = this.principalAttributeMap.get(ldapAttrName);
                if (attr.size() > 1) {
                    attributeMap.put(principalAttrName, attr.getStringValues());
                } else {
                    attributeMap.put(principalAttrName, attr.getStringValue());
                }
            }
        }
        return new SimplePrincipal(id, attributeMap);
    }

    /**
     * Initialize the handler, setup the authentication entry attributes.
     */
    @PostConstruct
    public void initialize() {
        initializeInternal();
        final List<String> attributes = new ArrayList<String>();
        if (this.principalIdAttribute != null) {
            attributes.add(this.principalIdAttribute);
        }
        attributes.addAll(this.principalAttributeMap.keySet());
        this.authenticatedEntryAttributes = attributes.toArray(new String[attributes.size()]);
    }

    /**
     * Initialize the handler internally.
     */
    protected void initializeInternal() {
    }
}
