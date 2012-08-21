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

package org.jasig.cas.web.view;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.QName;

import org.jasig.cas.authentication.principal.SamlService;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.util.CasHTTPSOAP11Encoder;
import org.jasig.cas.web.support.SamlArgumentExtractor;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml1.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.saml1.core.Response;
import org.opensaml.saml1.core.Status;
import org.opensaml.saml1.core.StatusCode;
import org.opensaml.saml1.core.StatusMessage;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.ConfigurationException;

/**
 * Base class for all views that render SAML1 SOAP messages directly to the HTTP response stream.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public abstract class AbstractSaml10ResponseView extends AbstractCasView {

    private static final String DEFAULT_ELEMENT_NAME_FIELD = "DEFAULT_ELEMENT_NAME";

    private static final String DEFAULT_ENCODING = "UTF-8";

    private final SamlArgumentExtractor samlArgumentExtractor = new SamlArgumentExtractor();

    private final HTTPSOAP11Encoder encoder = new CasHTTPSOAP11Encoder();

    private final SecureRandomIdentifierGenerator idGenerator;

    @NotNull
    private String encoding = DEFAULT_ENCODING;

    /**
     * Sets the character encoding in the HTTP response.
     *
     * @param encoding Response character encoding.
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    static {
        try {
            // Initialize OpenSAML default configuration
            // (only needed once per classloader)
            DefaultBootstrap.bootstrap();
        } catch (final ConfigurationException e) {
            throw new IllegalStateException("Error initializing OpenSAML library.", e);
        }
    }

    protected AbstractSaml10ResponseView() {
        try {
            this.idGenerator = new SecureRandomIdentifierGenerator();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot create secure random ID generator for SAML message IDs.");
        }
    }

    protected void renderMergedOutputModel(
            final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        response.setCharacterEncoding(this.encoding);

        final WebApplicationService service = this.samlArgumentExtractor.extractService(request);
        final String serviceId = service != null ? service.getId() : "UNKNOWN";

        try {
            final Response samlResponse = newSamlObject(Response.class);
            samlResponse.setID(generateId());
            samlResponse.setIssueInstant(new DateTime());
            samlResponse.setVersion(SAMLVersion.VERSION_11);
            samlResponse.setRecipient(serviceId);
            if (service instanceof SamlService) {
                final SamlService samlService = (SamlService) service;

                if (samlService.getRequestID() != null) {
                    samlResponse.setInResponseTo(samlService.getRequestID());
                }
            }
            prepareResponse(samlResponse, model);

            final BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();
            messageContext.setOutboundMessageTransport(new HttpServletResponseAdapter(response, request.isSecure()));
            messageContext.setOutboundSAMLMessage(samlResponse);
            this.encoder.encode(messageContext);
        } catch (final Exception e) {
            this.log.error("Error generating SAML response for service {}.", serviceId);
            throw e;
        }
    }

    /**
     * Subclasses must implement this method by adding child elements (status, assertion, etc) to
     * the given empty SAML 1 response message.  Impelmenters need not be concerned with error handling.
     *
     * @param response SAML 1 response message to be filled.
     * @param model Spring MVC model map containing data needed to prepare response.
     */
    protected abstract void prepareResponse(Response response, Map<String, Object> model);


    protected final String generateId() {
        return this.idGenerator.generateIdentifier();
    }

    protected final <T extends SAMLObject> T newSamlObject(final Class<T> objectType) {
        final QName qName;
        try {
            final Field f = objectType.getField(DEFAULT_ELEMENT_NAME_FIELD);
            qName = (QName) f.get(null);
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException("Cannot find field " + objectType.getName() + "." + DEFAULT_ELEMENT_NAME_FIELD);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Cannot access field " + objectType.getName() + "." + DEFAULT_ELEMENT_NAME_FIELD);
        }
        final SAMLObjectBuilder<T> builder = (SAMLObjectBuilder<T>) Configuration.getBuilderFactory().getBuilder(qName);
        if (builder == null) {
            throw new IllegalStateException("No SAMLObjectBuilder registered for class " + objectType.getName());
        }
        return objectType.cast(builder.buildObject());
    }

    protected final Status newStatus(final QName codeValue, final String statusMessage) {
        final Status status = newSamlObject(Status.class);
        final StatusCode code = newSamlObject(StatusCode.class);
        code.setValue(codeValue);
        status.setStatusCode(code);
        if (statusMessage != null) {
            final StatusMessage message = newSamlObject(StatusMessage.class);
            message.setMessage(statusMessage);
            status.setStatusMessage(message);
        }
        return status;
    }
}
