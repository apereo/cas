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

package org.jasig.cas.util;

import org.opensaml.Configuration;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml1.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.ws.soap.common.SOAPObjectBuilder;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.ws.soap.util.SOAPConstants;
import org.opensaml.xml.XMLObjectBuilderFactory;

/**
 * Override OpenSAML {@link HTTPSOAP11Encoder} such that SOAP-ENV XML namespace prefix is used for SOAP envelope
 * elements.  This is needed for backward compatibility with certain CAS clients (e.g. Java CAS client).
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public final class CasHTTPSOAP11Encoder extends HTTPSOAP11Encoder {
    private static final String OPENSAML_11_SOAP_NS_PREFIX = "SOAP-ENV";

    @Override
    protected Envelope buildSOAPMessage(final SAMLObject samlMessage) {
        final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

        final SOAPObjectBuilder<Envelope> envBuilder =
                (SOAPObjectBuilder<Envelope>) builderFactory.getBuilder(Envelope.DEFAULT_ELEMENT_NAME);
        final Envelope envelope = envBuilder.buildObject(
                SOAPConstants.SOAP11_NS, Envelope.DEFAULT_ELEMENT_LOCAL_NAME, OPENSAML_11_SOAP_NS_PREFIX);

        final SOAPObjectBuilder<Body> bodyBuilder =
                (SOAPObjectBuilder<Body>) builderFactory.getBuilder(Body.DEFAULT_ELEMENT_NAME);
        final Body body = bodyBuilder.buildObject(
                SOAPConstants.SOAP11_NS, Body.DEFAULT_ELEMENT_LOCAL_NAME, OPENSAML_11_SOAP_NS_PREFIX);

        body.getUnknownXMLObjects().add(samlMessage);
        envelope.setBody(body);

        return envelope;
    }
}
