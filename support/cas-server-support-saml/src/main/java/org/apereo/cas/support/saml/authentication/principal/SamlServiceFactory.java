package org.apereo.cas.support.saml.authentication.principal;

import org.apache.commons.lang3.NotImplementedException;
import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.stream.Collectors;

/**
 * The {@link SamlServiceFactory} creates {@link SamlService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlServiceFactory extends AbstractServiceFactory<SamlService> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlServiceFactory.class);
    
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
            LOGGER.debug("Request does not specify a [{}] or request body is empty", SamlProtocolConstants.CONST_PARAM_TARGET);
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
            requestId = extractRequestId(requestBody);
        } else {
            artifactId = null;
            requestId = null;
        }

        LOGGER.debug("Request Body: [{}]", requestBody);
        LOGGER.debug("Extracted ArtifactId: [{}]. extracted Request Id: [{}]", artifactId, requestId);

        return new SamlService(id, service, artifactId, requestId);
    }

    @Override
    public SamlService createService(final String id) {
        throw new NotImplementedException("This operation is not supported. ");
    }

    /**
     * Extract request id from the body.
     *
     * @param requestBody the request body
     * @return the string
     */
    private static String extractRequestId(final String requestBody) {
        if (!requestBody.contains("RequestID")) {
            LOGGER.debug("Request body does not contain a request id");
            return null;
        }

        try {
            final int position = requestBody.indexOf("RequestID=\"") + CONST_REQUEST_ID_LENGTH;
            final int nextPosition = requestBody.indexOf('"', position);

            return requestBody.substring(position, nextPosition);
        } catch (final Exception e) {
            LOGGER.debug("Exception parsing RequestID from request.", e);
            return null;
        }
    }

    /**
     * Gets the request body from the request.
     *
     * @param request the request
     * @return the request body
     */
    private static String getRequestBody(final HttpServletRequest request) {
        try(BufferedReader reader = request.getReader()) {
            if (reader == null) {
                LOGGER.debug("Request body could not be read because it's empty.");
                return null;
            }
            return reader.lines().collect(Collectors.joining());
        } catch (final Exception e) {
            LOGGER.trace("Could not obtain the saml request body from the http request", e);
            return null;
        }
    }
}
