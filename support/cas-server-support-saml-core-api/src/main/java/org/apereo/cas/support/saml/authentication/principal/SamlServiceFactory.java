package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.util.AbstractSamlObjectBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.jdom2.Element;
import org.jdom2.Namespace;
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

    /**
     * Gets the request body from the request.
     *
     * @param request the request
     * @return the request body
     */
    private static String getRequestBody(final HttpServletRequest request) {
        val body = readRequestBodyIfAny(request);
        if (!StringUtils.hasText(body)) {
            LOGGER.trace("Looking at the request attribute [{}] to locate SAML request body",
                SamlProtocolConstants.PARAMETER_SAML_REQUEST);
            return (String) request.getAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST);
        }
        return body;
    }

    private static String readRequestBodyIfAny(final HttpServletRequest request) {
        try (val reader = request.getReader()) {
            if (reader != null) {
                return reader.lines().collect(Collectors.joining());
            }
            LOGGER.trace("Request body could not be read because it's empty.");
        } catch (final Exception e) {
            LOGGER.trace("Could not obtain the saml request body from the http request", e);
        }
        return null;
    }

    @Override
    public SamlService createService(final HttpServletRequest request) {
        val service = request.getParameter(SamlProtocolConstants.CONST_PARAM_TARGET);
        val requestBody = request.getRequestURI().contains(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE)
            && request.getMethod().equalsIgnoreCase(HttpMethod.POST.name()) ? getRequestBody(request) : null;

        LOGGER.trace("Request Body: [{}]", requestBody);
        if (!StringUtils.hasText(service) && !StringUtils.hasText(requestBody)) {
            LOGGER.trace("Request does not specify a [{}] or request body is empty", SamlProtocolConstants.CONST_PARAM_TARGET);
            return null;
        }
        val id = cleanupUrl(service);

        if (StringUtils.hasText(requestBody)) {
            request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, requestBody);
        }
        val requestChild = getRequestDocumentElement(requestBody);
        val artifactId = getArtifactIdFromRequest(requestChild);
        val requestId = getRequestIdFromRequest(requestChild);
        LOGGER.trace("Extracted ArtifactId: [{}]. Extracted Request Id: [{}]", artifactId, requestId);

        val samlService = new SamlService(id, service, artifactId, requestId);
        samlService.setSource(SamlProtocolConstants.CONST_PARAM_TARGET);
        return samlService;
    }

    @Override
    public SamlService createService(final String id) {
        throw new NotImplementedException("This operation is not supported. ");
    }

    private static Element getRequestDocumentElement(final String requestBody) {
        if (StringUtils.hasText(requestBody)) {
            val document = AbstractSamlObjectBuilder.constructDocumentFromXml(requestBody);
            if (document == null) {
                LOGGER.trace("XML document could not extracted from request body [{}]", requestBody);
                return null;
            }

            val root = document.getRootElement();

            val body = root.getChild("Body", NAMESPACE_ENVELOPE);
            if (body == null) {
                LOGGER.trace("XML document root has no child body element");
                return null;
            }
            return body.getChild("Request", NAMESPACE_SAML1);
        }
        return null;
    }

    private static String getRequestIdFromRequest(final Element requestChild) {
        if (requestChild == null) {
            LOGGER.trace("Element responsible for RequestID is undefined");
            return null;
        }
        val requestIdAttribute = requestChild.getAttribute("RequestID");
        if (requestIdAttribute == null) {
            LOGGER.trace("XML element has no attribute for RequestID");
            return null;
        }
        return requestIdAttribute.getValue().trim();
    }

    private static String getArtifactIdFromRequest(final Element requestChild) {
        if (requestChild == null) {
            LOGGER.trace("Element responsible for AssertionArtifact is undefined");
            return null;
        }
        val artifactElement = requestChild.getChild("AssertionArtifact", NAMESPACE_SAML1);
        if (artifactElement == null) {
            LOGGER.trace("Element [{}] does not contain a child element for AssertionArtifact", requestChild.getName());
            return null;
        }
        return artifactElement.getValue().trim();
    }
}
