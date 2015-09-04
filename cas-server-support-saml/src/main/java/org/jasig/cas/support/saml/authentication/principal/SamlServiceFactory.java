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

package org.jasig.cas.support.saml.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractServiceFactory;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

/**
 * The {@link SamlServiceFactory} is responsible for...
 *
 * @author Misagh Moayyed
 */
public class SamlServiceFactory extends AbstractServiceFactory<SamlService> {

    private static final int CONST_REQUEST_ID_LENGTH = 11;

    private static final String CONST_START_ARTIFACT_XML_TAG_NO_NAMESPACE = "<AssertionArtifact>";

    private static final String CONST_END_ARTIFACT_XML_TAG_NO_NAMESPACE = "</AssertionArtifact>";

    private static final String CONST_START_ARTIFACT_XML_TAG = "<samlp:AssertionArtifact>";

    private static final String CONST_END_ARTIFACT_XML_TAG = "</samlp:AssertionArtifact>";

    @Override
    public SamlService createService(final HttpServletRequest request) {
        final String service = request.getParameter(SamlProtocolConstants.CONST_PARAM_TARGET);
        final String artifactId;
        final String requestBody = getRequestBody(request);
        final String requestId;

        if (!StringUtils.hasText(service) && !StringUtils.hasText(requestBody)) {
            logger.debug("Request does not specify a {} or request body is empty",
                    SamlProtocolConstants.CONST_PARAM_TARGET);
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

    @Override
    public SamlService createService(final String id) {
        return null;
    }

    /**
     * Extract request id from the body.
     *
     * @param requestBody the request body
     * @return the string
     */
    private String extractRequestId(final String requestBody) {
        if (!requestBody.contains("RequestID")) {
            logger.debug("Request body does not contain a request id");
            return null;
        }

        try {
            final int position = requestBody.indexOf("RequestID=\"") + CONST_REQUEST_ID_LENGTH;
            final int nextPosition = requestBody.indexOf('"', position);

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
    private String getRequestBody(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        try (final BufferedReader reader = request.getReader()) {

            if (reader == null) {
                logger.debug("Request body could not be read because it's empty.");
                return null;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (final Exception e) {
            logger.trace("Could not obtain the saml request body from the http request", e);
            return null;
        }
    }
}
