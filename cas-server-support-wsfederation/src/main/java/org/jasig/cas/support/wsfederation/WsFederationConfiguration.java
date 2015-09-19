/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.support.wsfederation;

import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class gathers configuration information for the WS Federation Identity Provider.
 *
 * @author John Gasper
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("wsFedConfig")
public final class WsFederationConfiguration implements Serializable {
    private static final long serialVersionUID = 2310859477512242659L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    @Value("${cas.wsfed.idp.idattribute:upn}")
    private String identityAttribute;

    @NotNull
    @Value("${cas.wsfed.idp.id}")
    private String identityProviderIdentifier;

    @NotNull
    @Value("${cas.wsfed.idp.url}")
    private String identityProviderUrl;

    @NotNull
    @Value("#{'${cas.wsfed.idp.signingcerts}'.split(',')}")
    private List<Resource> signingCertificateFiles;

    @NotNull
    @Value("${cas.wsfed.rp.id:urn:cas:localhost}")
    private String relyingPartyIdentifier;

    @Value("${cas.wsfed.idp.tolerance:10000}")
    private int tolerance;

    @Autowired(required=false)
    @Qualifier("wsfedAttributeMutator")
    private WsFederationAttributeMutator attributeMutator;

    private List<Credential> signingWallet;

    /**
     * gets the identity of the IdP.
     *
     * @return the identity
     */
    public String getIdentityAttribute() {
        return this.identityAttribute;
    }

    /**
     * sets the identity of the IdP.
     *
     * @param identityAttribute the identity
     */
    public void setIdentityAttribute(final String identityAttribute) {
        this.identityAttribute = identityAttribute;
    }

    /**
     * gets the identity provider identifier.
     *
     * @return the identifier
     */
    public String getIdentityProviderIdentifier() {
        return identityProviderIdentifier;
    }

    /**
     * sets the identity provider identifier.
     *
     * @param identityProviderIdentifier the identifier.
     */
    public void setIdentityProviderIdentifier(final String identityProviderIdentifier) {
        this.identityProviderIdentifier = identityProviderIdentifier;
    }

    /**
     * gets the identity provider url.
     *
     * @return the url
     */
    public String getIdentityProviderUrl() {
        return this.identityProviderUrl;
    }

    /**
     * sets the identity provider url.
     *
     * @param identityProviderUrl the url
     */
    public void setIdentityProviderUrl(final String identityProviderUrl) {
        this.identityProviderUrl = identityProviderUrl;
    }

    /**
     * gets the relying part identifier.
     *
     * @return the identifier
     */
    public String getRelyingPartyIdentifier() {
        return this.relyingPartyIdentifier;
    }

    /**
     * sets the relying party identifier.
     *
     * @param relyingPartyIdentifier the identifier
     */
    public void setRelyingPartyIdentifier(final String relyingPartyIdentifier) {
        this.relyingPartyIdentifier = relyingPartyIdentifier;
    }

    /**
     * gets the signing certificates.
     *
     * @return X509credentials of the signing certs
     */
    public List<Credential> getSigningCertificates() {
        return this.signingWallet;
    }

    /**
     * gets the list of signing certificate files.
     *
     * @return the list of files
     */
    public List<Resource> getSigningCertificateFiles() {
        return this.signingCertificateFiles;
    }

    /**
     * sets the signing certs.
     *
     * @param signingCertificateFiles a list of certificate files to read in.
     */
    public void setSigningCertificateFiles(final Resource... signingCertificateFiles) {
        this.signingCertificateFiles = Arrays.asList(signingCertificateFiles);

        final List<Credential> signingCerts = new ArrayList<>();

        for (final Resource file : signingCertificateFiles) {
            signingCerts.add(getSigningCredential(file));
        }

        this.signingWallet = signingCerts;
    }

    /**
     * gets the tolerance.
     *
     * @return the tolerance in milliseconds
     */
    public int getTolerance() {
        return tolerance;
    }

    /**
     * sets the tolerance of the validity of the timestamp token.
     *
     * @param tolerance the tolerance in milliseconds
     */
    public void setTolerance(final int tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * gets the attributeMutator.
     *
     * @return an attributeMutator
     */
    public WsFederationAttributeMutator getAttributeMutator() {
        return attributeMutator;
    }

    /**
     * sets the attributeMutator.
     *
     * @param attributeMutator an attributeMutator
     */
    public void setAttributeMutator(final WsFederationAttributeMutator attributeMutator) {
        this.attributeMutator = attributeMutator;
    }

    /**
     * getSigningCredential loads up an X509Credential from a file.
     *
     * @param resource the signing certificate file
     * @return an X509 credential
     */
    private Credential getSigningCredential(final Resource resource) {
        try (final InputStream inputStream = resource.getInputStream()) {
            final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            final X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            final Credential publicCredential = new BasicX509Credential(certificate);
            logger.debug("getSigningCredential: key retrieved.");
            return publicCredential;
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }
}
