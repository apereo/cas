package org.apereo.cas.support.saml.web.view;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.support.saml.authentication.SamlResponseBuilder;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.saml1.core.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    protected AbstractSaml10ResponseView(final boolean successResponse,
                                         final ProtocolAttributeEncoder protocolAttributeEncoder,
                                         final ServicesManager servicesManager,
                                         final ArgumentExtractor samlArgumentExtractor,
                                         final AuthenticationAttributeReleasePolicy authAttrReleasePolicy,
                                         final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                         final CasProtocolAttributesRenderer attributesRenderer,
                                         final SamlResponseBuilder samlResponseBuilder,
                                         final AttributeDefinitionStore attributeDefinitionStore) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authAttrReleasePolicy,
            serviceSelectionStrategy, attributesRenderer, attributeDefinitionStore);
        this.samlArgumentExtractor = samlArgumentExtractor;
        this.samlResponseBuilder = samlResponseBuilder;
    }

    @Override
    protected void renderMergedOutputModel(
        @NonNull final Map<String, Object> model,
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response) throws Exception {
        try {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            val service = this.samlArgumentExtractor.extractService(request);
            val recipient = getResponseRecipient(service);
            LOGGER.debug("Using [{}] as the recipient of the SAML response for [{}]", recipient, service);
            val samlResponse = samlResponseBuilder.createResponse(recipient, service);
            prepareResponse(samlResponse, model);
            finalizeSamlResponse(request, response, recipient, samlResponse);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, "Error generating SAML response for service", e);
            throw e;
        }
    }

    /**
     * Finalize saml response.
     *
     * @param request      the request
     * @param response     the response
     * @param recipient    the URI of the intended recipient of this response
     * @param samlResponse the saml response
     * @throws Exception the exception
     */
    protected void finalizeSamlResponse(final HttpServletRequest request, final HttpServletResponse response,
                                        final @Nullable String recipient, final Response samlResponse) throws Exception {
        if (request != null && response != null) {
            LOGGER.debug("Starting to encode SAML response for recipient [{}]", recipient);
            samlResponseBuilder.encodeSamlResponse(samlResponse, request, response);
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

    /**
     * The recipient of a SAML1 response is the URI of the party the response is intended for, so it
     * is the service URL itself. It is left undefined when the request did not identify a service,
     * rather than being filled in with a placeholder.
     *
     * @param service the service the response is being produced for
     * @return the recipient URI, or null when there is no service to name
     */
    private static @Nullable String getResponseRecipient(final @Nullable Service service) {
        return service != null && StringUtils.isNotBlank(service.getId()) ? service.getId() : null;
    }
}
