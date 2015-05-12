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

package org.jasig.cas.support.saml.authentication;

import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.jasig.cas.util.CompressionUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

import static org.junit.Assert.*;

/**
 * Utility class to ensure authentication requests are properly encoded and decoded.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class SamlAuthenticationRequestTests extends AbstractOpenSamlTests {
    private static final String SAML_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
            + "ID=\"5545454455\" Version=\"2.0\" IssueInstant=\"Value\" "
            + "ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" "
            + "ProviderName=\"https://localhost:8443/myRutgers\" "
            + "AssertionConsumerServiceURL=\"https://localhost:8443/myRutgers\"/>";

    @Test
    public void ensureDeflation() throws Exception {
        final String deflator = CompressionUtils.deflate(SAML_REQUEST);
        final String deflatorStream = deflateViaStream(SAML_REQUEST);
        assertEquals(deflatorStream, deflator);
    }

    @Test
    public void ensureInflation() throws Exception {
        final String deflator = CompressionUtils.deflate(SAML_REQUEST);
        final AbstractSaml20ObjectBuilder builder = new GoogleSaml20ObjectBuilder();
        final String msg = builder.decodeSamlAuthnRequest(deflator);
        assertEquals(msg, SAML_REQUEST);
    }

    private String deflateViaStream(final String samlRequest) throws IOException {
        final byte[] xmlBytes = samlRequest.getBytes("UTF-8");
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        final DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(
                byteOutputStream);
        deflaterOutputStream.write(xmlBytes, 0, xmlBytes.length);
        deflaterOutputStream.close();
        return CompressionUtils.encodeBase64(byteOutputStream.toByteArray());
    }
}
