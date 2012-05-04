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

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.principal.Credentials;
import org.opensaml.SAMLAuthenticationStatement;

/**
 * AuthenticationMetaDataPopulator to retrieve the Authentication Type.
 * <p>
 * Note: Authentication Methods are exposed under the key:
 * <code>samlAuthenticationStatement::authMethod</code> in the Authentication
 * attributes map.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class SamlAuthenticationMetaDataPopulator implements
    AuthenticationMetaDataPopulator {

    public static final String ATTRIBUTE_AUTHENTICATION_METHOD = "samlAuthenticationStatementAuthMethod";

    private Map<String, String> authenticationMethods = new HashMap<String, String>();

    public SamlAuthenticationMetaDataPopulator() {
        this.authenticationMethods
            .put(
                "org.jasig.cas.authentication.principal.HttpBasedServiceCredentials",
                SAMLAuthenticationStatement.AuthenticationMethod_SSL_TLS_Client);
        this.authenticationMethods
            .put(
                "org.jasig.cas.authentication.principal.UsernamePasswordCredentials",
                SAMLAuthenticationStatement.AuthenticationMethod_Password);
        this.authenticationMethods
            .put(
                "org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredentials",
                SAMLAuthenticationStatement.AuthenticationMethod_Unspecified);
        this.authenticationMethods
            .put(
                "org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials",
                SAMLAuthenticationStatement.AuthenticationMethod_X509_PublicKey);
    }

    public final Authentication populateAttributes(
        final Authentication authentication, final Credentials credentials) {

        final String credentialsClass = credentials.getClass().getName();
        final String authenticationMetehod = this.authenticationMethods
            .get(credentialsClass);

        authentication.getAttributes().put(ATTRIBUTE_AUTHENTICATION_METHOD,
            authenticationMetehod);

        return authentication;
    }

    /**
     * Map of user-defined mappings. Note it is possible to over-ride the
     * defaults. Mapping should be of the following type:
     * <p>(<String version of Package/Class Name> <SAML Type>)
     * <p>
     * Example: (<"org.jasig.cas.authentication.principal.HttpBasedServiceCredentials">
     * <SAMLAuthenticationStatement.AuthenticationMethod_SSL_TLS_Client>)
     * 
     * @param userDefinedMappings map of user defined authentication types.
     */
    public void setUserDefinedMappings(final Map<String, String> userDefinedMappings) {
        this.authenticationMethods.putAll(userDefinedMappings);
    }
}
