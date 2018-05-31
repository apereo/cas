package org.apereo.cas.support.saml.authentication.principal;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.jdom.Namespace;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * The {@link SamlServiceFactory} creates {@link SamlService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@RequiredArgsConstructor
public class SamlServiceFactory extends AbstractServiceFactory<SamlService> {
    private static final Namespace NAMESPACE_ENVELOPE = Namespace.getNamespace("http://schemas.xmlsoap.org/soap/envelope/");
    private static final Namespace NAMESPACE_SAML1 = Namespace.getNamespace("urn:oasis:names:tc:SAML:1.0:protocol");

    private final Saml10ObjectBuilder saml10ObjectBuilder;

    @Override
    public SamlService createService(final HttpServletRequest request) {
        final var service = request.getParameter(SamlProtocolConstants.CONST_PARAM_TARGET);
        final var requestBody = request.getMethod().equalsIgnoreCase(HttpMethod.POST.name()) ? getRequestBody(request) : null;
        final String artifactId;
        final String requestId;

        if (!StringUtils.hasText(service) && !StringUtils.hasText(requestBody)) {
            LOGGER.trace("Request does not specify a [{}] or request body is empty", SamlProtocolConstants.CONST_PARAM_TARGET);
            return null;
        }
        final var id = cleanupUrl(service);

        if (StringUtils.hasText(requestBody)) {
            request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, requestBody);

            final var document = saml10ObjectBuilder.constructDocumentFromXml(requestBody);
            final var root = document.getRootElement();

            @NonNull
            final var body = root.getChild("Body", NAMESPACE_ENVELOPE);
            @NonNull
            final var requestChild = body.getChild("Request", NAMESPACE_SAML1);

            @NonNull
            final var artifactElement = requestChild.getChild("AssertionArtifact", NAMESPACE_SAML1);
            artifactId = artifactElement.getValue();

            @NonNull
            final var requestIdAttribute = requestChild.getAttribute("RequestID");
            requestId = requestIdAttribute.getValue();
        } else {
            artifactId = null;
            requestId = null;
        }

        LOGGER.debug("Request Body: [{}]\n\"Extracted ArtifactId: [{}]. Extracted Request Id: [{}]", requestBody, artifactId, requestId);
        final var samlService = new SamlService(id, service, artifactId, requestId);
        samlService.setSource(SamlProtocolConstants.CONST_PARAM_TARGET);
        return samlService;
    }

    @Override
    public SamlService createService(final String id) {
        throw new NotImplementedException("This operation is not supported. ");
    }

    /**
     * Gets the request body from the request.
     *
     * @param request the request
     * @return the request body
     */
    private static String getRequestBody(final HttpServletRequest request) {
        String body = null;
        try (var reader = request.getReader()) {
            if (reader == null) {
                LOGGER.debug("Request body could not be read because it's empty.");
            } else {
                body = reader.lines().collect(Collectors.joining());
            }
        } catch (final Exception e) {
            LOGGER.trace("Could not obtain the saml request body from the http request", e);
        }

        if (!StringUtils.hasText(body)) {
            LOGGER.trace("Looking at the request attribute [{}] to locate SAML request body", SamlProtocolConstants.PARAMETER_SAML_REQUEST);
            body = (String) request.getAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST);
            LOGGER.trace("Located cached saml request body [{}] as a request attribute", body);
        }
        return body;
    }
}
