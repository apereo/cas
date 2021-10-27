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
package org.jasig.cas.support.saml.authentication;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;

/**
 * AuthenticationMetaDataPopulator to retrieve the Authentication Type.
 * <p>
 * Note: Authentication Methods are exposed under the key:
 * <code>samlAuthenticationStatement::authMethod</code> in the Authentication
 * attributes map.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public class SamlAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    public static final String ATTRIBUTE_AUTHENTICATION_METHOD = "samlAuthenticationStatementAuthMethod";

    public static final String AUTHN_METHOD_PASSWORD = "urn:oasis:names:tc:SAML:1.0:am:password";

    public static final String AUTHN_METHOD_SSL_TLS_CLIENT = "urn:ietf:rfc:2246";

    public static final String AUTHN_METHOD_X509_PUBLICKEY = "urn:oasis:names:tc:SAML:1.0:am:X509-PKI";

    public static final String AUTHN_METHOD_UNSPECIFIED = "urn:oasis:names:tc:SAML:1.0:am:unspecified";

    private final Map<String, String> authenticationMethods = new HashMap<String, String>();

    public SamlAuthenticationMetaDataPopulator() {
        this.authenticationMethods.put(
                HttpBasedServiceCredential.class.getName(),
                AUTHN_METHOD_SSL_TLS_CLIENT);
        this.authenticationMethods.put(
                UsernamePasswordCredential.class.getName(),
                AUTHN_METHOD_PASSWORD);

        // Next two classes are in other modules, so avoid using Class#getName() to prevent circular dependency
        this.authenticationMethods.put(
                "org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredentials",
                AUTHN_METHOD_UNSPECIFIED);
        this.authenticationMethods.put(
                "org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials",
                AUTHN_METHOD_X509_PUBLICKEY);
    }

    @Override
    public final void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {

        final String credentialsClass = credential.getClass().getName();
        final String authenticationMethod = this.authenticationMethods.get(credentialsClass);

        builder.addAttribute(ATTRIBUTE_AUTHENTICATION_METHOD, authenticationMethod);
    }

    /**
     * Map of user-defined mappings. Note it is possible to over-ride the
     * defaults. Mapping should be of the following type:
     * <p>(<String version of Package/Class Name> <SAML Type>)
     * <p>
     * Example: (<"org.jasig.cas.authentication.HttpBasedServiceCredential">
     * <SAMLAuthenticationStatement.AuthenticationMethod_SSL_TLS_Client>)
     *
     * @param userDefinedMappings map of user defined authentication types.
     */
    public void setUserDefinedMappings(final Map<String, String> userDefinedMappings) {
        this.authenticationMethods.putAll(userDefinedMappings);
    }
}
