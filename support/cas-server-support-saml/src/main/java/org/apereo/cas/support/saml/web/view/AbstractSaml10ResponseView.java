package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.support.saml.authentication.SamlResponseBuilder;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml1.core.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Base class for all views that render SAML1 SOAP messages directly to the HTTP response stream.
 *
 * @author Marvin S.Addison
 * @since 3.5.1
 */

@Slf4j
public abstract class AbstractSaml10ResponseView extends AbstractCasView {

    /**
     * SAML1 response builder.
     */
    protected final SamlResponseBuilder samlResponseBuilder;

    private final ArgumentExtractor samlArgumentExtractor;

    private final String encoding;


    public AbstractSaml10ResponseView(final boolean successResponse,
                                      final ProtocolAttributeEncoder protocolAttributeEncoder,
                                      final ServicesManager servicesManager,
                                      final ArgumentExtractor samlArgumentExtractor,
                                      final String encoding,
                                      final AuthenticationAttributeReleasePolicy authAttrReleasePolicy,
                                      final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                      final CasProtocolAttributesRenderer attributesRenderer,
                                      final SamlResponseBuilder samlResponseBuilder) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authAttrReleasePolicy, serviceSelectionStrategy, attributesRenderer);
        this.samlArgumentExtractor = samlArgumentExtractor;
        this.encoding = encoding;
        this.samlResponseBuilder = samlResponseBuilder;
    }

    @Override
    protected void renderMergedOutputModel(final Map<String, Object> model,
                                           final HttpServletRequest request,
                                           final HttpServletResponse response) throws Exception {
        try {
            response.setCharacterEncoding(this.encoding);
            val service = this.samlArgumentExtractor.extractService(request);
            val serviceId = getServiceIdFromRequest(service);
            LOGGER.debug("Using [{}] as the recipient of the SAML response for [{}]", serviceId, service);
            val samlResponse = samlResponseBuilder.createResponse(serviceId, service);
            prepareResponse(samlResponse, model);
            finalizeSamlResponse(request, response, serviceId, samlResponse);
        } catch (final Exception e) {
            LOGGER.error("Error generating SAML response for service", e);
            throw e;
        }
    }

    /**
     * Finalize saml response.
     *
     * @param request      the request
     * @param response     the response
     * @param serviceId    the service id
     * @param samlResponse the saml response
     * @throws Exception the exception
     */
    protected void finalizeSamlResponse(final HttpServletRequest request, final HttpServletResponse response,
                                        final String serviceId, final Response samlResponse) throws Exception {
        if (request != null && response != null) {
            LOGGER.debug("Starting to encode SAML response for service [{}]", serviceId);
            this.samlResponseBuilder.encodeSamlResponse(samlResponse, request, response);
        }
    }

    /**
     * Subclasses must implement this method by adding child elements (status, assertion, etc) to
     * the given empty SAML 1 response message.  Implementers need not be concerned with error handling.
     *
     * @param response SAML 1 response message to be filled.
     * @param model    Spring MVC model map containing data needed to prepare response.
     */
    protected abstract void prepareResponse(Response response, Map<String, Object> model);

    private static String getServiceIdFromRequest(final Service service) {
        if (service == null || StringUtils.isBlank(service.getId())) {
            return "UNKNOWN";
        }
        try {
            return new URL(service.getId()).getHost();
        } catch (final MalformedURLException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }
}
