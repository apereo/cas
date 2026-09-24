package org.apereo.cas.support.saml.authentication.principal;

import module java.base;
import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.util.AbstractSamlObjectBuilder;
import org.apereo.cas.web.UrlValidator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The {@link SamlServiceFactory} creates {@link SamlService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class SamlServiceFactory extends AbstractServiceFactory<SamlService> {
    private static final Namespace NAMESPACE_ENVELOPE = Namespace.getNamespace("http://schemas.xmlsoap.org/soap/envelope/");

    private static final Namespace NAMESPACE_SAML1 = Namespace.getNamespace("urn:oasis:names:tc:SAML:1.0:protocol");

    private static final String ATTRIBUTE_SAML_REQUEST_DETAILS = SamlServiceFactory.class.getName() + ".samlRequestDetails";

    public SamlServiceFactory(final TenantExtractor tenantExtractor,
                              final UrlValidator urlValidator) {
        super(tenantExtractor, urlValidator);
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
        val requestDetails = getSamlRequestDetails(request, requestBody);
        LOGGER.trace("Extracted ArtifactId: [{}]. Extracted Request Id: [{}]",
            requestDetails.artifactId(), requestDetails.requestId());

        val samlService = new SamlService(id, service, requestDetails.artifactId(), requestDetails.requestId());
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
            return reader.lines().collect(Collectors.joining(" "));
        } catch (final Exception e) {
            LOGGER.trace("Could not obtain the saml request body from the http request", e);
        }
        return null;
    }

    /**
     * Resolve the artifact and request identifiers carried by the SOAP body, parsing it at most once
     * per request. This factory is consulted several times while a single request is handled, by
     * request filters and interceptors, by the validation controller and again while the response
     * view is rendered, and the body cannot change in between, so the outcome of the parse is kept
     * on the request next to the body itself.
     *
     * @param request     the request being handled
     * @param requestBody the SOAP body, which may be absent
     * @return the identifiers found in the body, each of which may be null
     */
    private static SamlRequestDetails getSamlRequestDetails(final HttpServletRequest request, final String requestBody) {
        if (request.getAttribute(ATTRIBUTE_SAML_REQUEST_DETAILS) instanceof final SamlRequestDetails cached) {
            LOGGER.trace("Reusing SAML request details already resolved for this request");
            return cached;
        }
        val requestChild = getRequestDocumentElement(requestBody);
        val requestDetails = new SamlRequestDetails(getArtifactIdFromRequest(requestChild), getRequestIdFromRequest(requestChild));
        request.setAttribute(ATTRIBUTE_SAML_REQUEST_DETAILS, requestDetails);
        return requestDetails;
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

    private record SamlRequestDetails(String artifactId, String requestId) {
    }
}
