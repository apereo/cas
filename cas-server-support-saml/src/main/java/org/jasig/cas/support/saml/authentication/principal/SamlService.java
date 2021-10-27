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
package org.jasig.cas.support.saml.authentication.principal;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Class to represent that this service wants to use SAML. We use this in
 * combination with the CentralAuthenticationServiceImpl to choose the right
 * UniqueTicketIdGenerator.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class SamlService extends AbstractWebApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlService.class);

    /** Constant representing service. */
    private static final String CONST_PARAM_SERVICE = "TARGET";

    /** Constant representing artifact. */
    private static final String CONST_PARAM_TICKET = "SAMLart";

    private static final String CONST_START_ARTIFACT_XML_TAG_NO_NAMESPACE = "<AssertionArtifact>";

    private static final String CONST_END_ARTIFACT_XML_TAG_NO_NAMESPACE = "</AssertionArtifact>";

    private static final String CONST_START_ARTIFACT_XML_TAG = "<samlp:AssertionArtifact>";

    private static final String CONST_END_ARTIFACT_XML_TAG = "</samlp:AssertionArtifact>";

    private String requestId;

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -6867572626767140223L;

    protected SamlService(final String id) {
        super(id, id, null);
    }

    protected SamlService(final String id, final String originalUrl,
            final String artifactId, final String requestId) {
        super(id, originalUrl, artifactId);
        this.requestId = requestId;
    }

    /**
     * {@inheritDoc}
     * This always returns true because a SAML Service does not receive the TARGET value on validation.
     */
    @Override
    public boolean matches(final Service service) {
        return true;
    }

    public String getRequestID() {
        return this.requestId;
    }

    public static SamlService createServiceFrom(
            final HttpServletRequest request) {
        final String service = request.getParameter(CONST_PARAM_SERVICE);
        final String artifactId;
        final String requestBody = getRequestBody(request);
        final String requestId;

        if (!StringUtils.hasText(service) && !StringUtils.hasText(requestBody)) {
            return null;
        }

        final String id = cleanupUrl(service);

        if (StringUtils.hasText(requestBody)) {

            final String tagStart;
            final String tagEnd;
            if (requestBody.contains(CONST_START_ARTIFACT_XML_TAG)) {
                tagStart = CONST_START_ARTIFACT_XML_TAG;
                tagEnd = CONST_END_ARTIFACT_XML_TAG;
            } else {
                tagStart = CONST_START_ARTIFACT_XML_TAG_NO_NAMESPACE;
                tagEnd = CONST_END_ARTIFACT_XML_TAG_NO_NAMESPACE;
            }
            final int startTagLocation = requestBody.indexOf(tagStart);
            final int artifactStartLocation = startTagLocation + tagStart.length();
            final int endTagLocation = requestBody.indexOf(tagEnd);

            artifactId = requestBody.substring(artifactStartLocation, endTagLocation).trim();

            // is there a request id?
            requestId = extractRequestId(requestBody);
        } else {
            artifactId = null;
            requestId = null;
        }

        LOGGER.debug("Attempted to extract Request from HttpServletRequest. Results:");
        LOGGER.debug(String.format("Request Body: %s", requestBody));
        LOGGER.debug(String.format("Extracted ArtifactId: %s", artifactId));
        LOGGER.debug(String.format("Extracted Request Id: %s", requestId));

        return new SamlService(id, service, artifactId, requestId);
    }

    @Override
    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<String, String>();

        parameters.put(CONST_PARAM_TICKET, ticketId);
        parameters.put(CONST_PARAM_SERVICE, getOriginalUrl());

        return Response.getRedirectResponse(getOriginalUrl(), parameters);
    }

    protected static String extractRequestId(final String requestBody) {
        if (!requestBody.contains("RequestID")) {
            return null;
        }

        try {
            final int position = requestBody.indexOf("RequestID=\"") + 11;
            final int nextPosition = requestBody.indexOf("\"", position);

            return requestBody.substring(position,  nextPosition);
        } catch (final Exception e) {
            LOGGER.debug("Exception parsing RequestID from request.", e);
            return null;
        }
    }

    protected static String getRequestBody(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        try {
            final BufferedReader reader = request.getReader();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (final Exception e) {
            return null;
        }
    }
}
