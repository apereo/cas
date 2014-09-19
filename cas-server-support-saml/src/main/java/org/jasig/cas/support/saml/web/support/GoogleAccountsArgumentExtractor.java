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
package org.jasig.cas.support.saml.web.support;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.authentication.principal.GoogleAccountsService;
import org.jasig.cas.support.saml.util.SamlUtils;
import org.jasig.cas.web.support.AbstractArgumentExtractor;
import org.jdom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Constructs a GoogleAccounts compatible service and provides the public and
 * private keys.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class GoogleAccountsArgumentExtractor extends AbstractArgumentExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAccountsArgumentExtractor.class);

    @NotNull
    private final PublicKey publicKey;

    @NotNull
    private final PrivateKey privateKey;

    @NotNull
    private final ServicesManager servicesManager;
    
    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        return createServiceFrom(request, this.privateKey, this.publicKey, this.servicesManager);
    }

    /**
     * Instantiates a new google accounts argument extractor.
     *
     * @param publicKey the public key
     * @param privateKey the private key
     * @param servicesManager the services manager
     */
    public GoogleAccountsArgumentExtractor(final PublicKey publicKey,
            final PrivateKey privateKey, final ServicesManager servicesManager) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.servicesManager = servicesManager;
    }

    /**
     * @deprecated As of 4.1. Use Ctors instead.
     * @param privateKey the private key object
     */
    @Deprecated
    public void setPrivateKey(final PrivateKey privateKey) {
        LOGGER.warn("setPrivateKey() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1. Use Ctors instead.
     * @param publicKey the public key object
     */
    @Deprecated
    public void setPublicKey(final PublicKey publicKey) {
        LOGGER.warn("setPublicKey() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1. The behavior is controlled by the service registry instead.
     * Sets an alternate username to send to Google (i.e. fully qualified email address).  Relies on an appropriate
     * attribute available for the user.
     * <p>
     * Note that this is optional and the default is to use the normal identifier.
     *
     * @param alternateUsername the alternate username. This is OPTIONAL.
     */
    @Deprecated
    public void setAlternateUsername(final String alternateUsername) {
        LOGGER.warn("setAlternateUsername() is deprecated and has no effect. Instead use the configuration in service registry.");
    }

    /**
     * Creates the service from request.
     *
     * @param request the request
     * @param privateKey the private key
     * @param publicKey the public key
     * @param servicesManager the services manager
     * @return the google accounts service
     */
    protected GoogleAccountsService createServiceFrom(
            final HttpServletRequest request, final PrivateKey privateKey,
            final PublicKey publicKey, final ServicesManager servicesManager) {
        final String relayState = request.getParameter(SamlProtocolConstants.SAML2_RELAY_STATE);

        final String xmlRequest = decodeAuthnRequestXML(request.getParameter(SamlProtocolConstants.SAML2_PARAM_SERVICE));

        if (!StringUtils.hasText(xmlRequest)) {
            return null;
        }

        final Document document = SamlUtils.constructDocumentFromXmlString(xmlRequest);

        if (document == null) {
            return null;
        }

        final String assertionConsumerServiceUrl = document.getRootElement().getAttributeValue("AssertionConsumerServiceURL");
        final String requestId = document.getRootElement().getAttributeValue("ID");

        return new GoogleAccountsService(assertionConsumerServiceUrl,
                relayState, requestId, privateKey, publicKey, servicesManager);
    }

    /**
     * Deflate the given bytes using zlib.
     *
     * @param bytes the bytes
     * @return the converted string
     */
    private String zlibDeflate(final byte[] bytes) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final InflaterInputStream iis = new InflaterInputStream(bais);
        final byte[] buf = new byte[1024];

        try {
            int count = iis.read(buf);
            while (count != -1) {
                baos.write(buf, 0, count);
                count = iis.read(buf);
            }
            return new String(baos.toByteArray());
        } catch (final Exception e) {
            return null;
        } finally {
            IOUtils.closeQuietly(iis);
        }
    }

    /**
     * Base64 decode.
     *
     * @param xml the xml
     * @return the byte[]
     */
    private byte[] base64Decode(final String xml) {
        try {
            final byte[] xmlBytes = xml.getBytes("UTF-8");
            return Base64.decodeBase64(xmlBytes);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Decode authn request xml.
     *
     * @param encodedRequestXmlString the encoded request xml string
     * @return the request
     */
    private String decodeAuthnRequestXML(
            final String encodedRequestXmlString) {
        if (encodedRequestXmlString == null) {
            return null;
        }

        final byte[] decodedBytes = base64Decode(encodedRequestXmlString);

        if (decodedBytes == null) {
            return null;
        }

        final String inflated = inflate(decodedBytes);

        if (inflated != null) {
            return inflated;
        }

        return zlibDeflate(decodedBytes);
    }

    /**
     * Inflate the given byte array.
     *
     * @param bytes the bytes
     * @return the string
     */
    private String inflate(final byte[] bytes) {
        final Inflater inflater = new Inflater(true);
        final byte[] xmlMessageBytes = new byte[10000];

        final byte[] extendedBytes = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, extendedBytes, 0, bytes.length);
        extendedBytes[bytes.length] = 0;

        inflater.setInput(extendedBytes);

        try {
            final int resultLength = inflater.inflate(xmlMessageBytes);
            inflater.end();

            if (!inflater.finished()) {
                throw new RuntimeException("buffer not large enough.");
            }

            inflater.end();
            return new String(xmlMessageBytes, 0, resultLength, "UTF-8");
        } catch (final DataFormatException e) {
            return null;
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot find encoding: UTF-8", e);
        }
    }
}
