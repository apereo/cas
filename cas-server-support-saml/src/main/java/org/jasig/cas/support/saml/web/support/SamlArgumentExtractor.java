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

import org.apache.commons.io.IOUtils;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.authentication.principal.SamlService;
import org.jasig.cas.web.support.AbstractArgumentExtractor;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

/**
 * Retrieve the ticket and artifact based on the SAML 1.1 profile.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class SamlArgumentExtractor extends AbstractArgumentExtractor {

    private static final String CONST_START_ARTIFACT_XML_TAG_NO_NAMESPACE = "<AssertionArtifact>";

    private static final String CONST_END_ARTIFACT_XML_TAG_NO_NAMESPACE = "</AssertionArtifact>";

    private static final String CONST_START_ARTIFACT_XML_TAG = "<samlp:AssertionArtifact>";

    private static final String CONST_END_ARTIFACT_XML_TAG = "</samlp:AssertionArtifact>";

    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        return createServiceFrom(request);
    }

    /**
     * Creates the SAML service from the request.
     *
     * @param request the request
     * @return the SAML service
     */
    protected SamlService createServiceFrom(final HttpServletRequest request) {
        final String service = request.getParameter(SamlProtocolConstants.SAML1_PARAM_SERVICE);
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

        logger.debug("Attempted to extract Request from HttpServletRequest. Results:");
        logger.debug("Request Body: {}", requestBody);
        logger.debug("Extracted ArtifactId: {}", artifactId);
        logger.debug("Extracted Request Id: {}", requestId);

        return new SamlService(id, service, artifactId, requestId);
    }

    /**
     * Extract request id from the body.
     *
     * @param requestBody the request body
     * @return the string
     */
    protected String extractRequestId(final String requestBody) {
        if (!requestBody.contains("RequestID")) {
            return null;
        }

        try {
            final int position = requestBody.indexOf("RequestID=\"") + 11;
            final int nextPosition = requestBody.indexOf("\"", position);

            return requestBody.substring(position,  nextPosition);
        } catch (final Exception e) {
            logger.debug("Exception parsing RequestID from request.", e);
            return null;
        }
    }

    /**
     * Gets the request body from the request.
     *
     * @param request the request
     * @return the request body
     */
    protected String getRequestBody(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = request.getReader();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (final Exception e) {
            return null;
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
