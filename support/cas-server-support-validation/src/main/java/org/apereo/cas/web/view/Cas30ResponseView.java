package org.apereo.cas.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders and prepares CAS3 views. This view is responsible
 * to simply just prep the base model, and delegates to
 * a the real view to render the final output.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
public class Cas30ResponseView extends Cas20ResponseView {
    private final CasProtocolAttributesRenderer attributesRenderer;

    public Cas30ResponseView(final boolean successResponse,
                             final ProtocolAttributeEncoder protocolAttributeEncoder,
                             final ServicesManager servicesManager,
                             final View view,
                             final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                             final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                             final CasProtocolAttributesRenderer attributesRenderer) {
        super(successResponse, protocolAttributeEncoder, servicesManager, view,
            authenticationAttributeReleasePolicy, serviceSelectionStrategy);
        this.attributesRenderer = attributesRenderer;
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        super.prepareMergedOutputModel(model, request, response);

        val service = authenticationRequestServiceSelectionStrategies.resolveService(getServiceFrom(model));
        val registeredService = this.servicesManager.findServiceBy(service);

        val principalAttributes = getCasPrincipalAttributes(model, registeredService);
        val attributes = new HashMap<String, Object>(principalAttributes);

        LOGGER.debug("Processed principal attributes from the output model to be [{}]", principalAttributes.keySet());
        val protocolAttributes = getCasProtocolAuthenticationAttributes(model, registeredService);
        attributes.putAll(protocolAttributes);

        LOGGER.debug("Final collection of attributes for the response are [{}].", attributes.keySet());
        putCasResponseAttributesIntoModel(model, attributes, registeredService);
    }

    /**
     * Put cas principal attributes into model.
     *
     * @param model             the model
     * @param registeredService the registered service
     * @return the cas principal attributes
     */
    protected Map<String, Object> getCasPrincipalAttributes(final Map<String, Object> model, final RegisteredService registeredService) {
        return super.getPrincipalAttributesAsMultiValuedAttributes(model);
    }

    /**
     * Put cas response attributes into model.
     *
     * @param model             the model
     * @param attributes        the attributes
     * @param registeredService the registered service
     */
    protected void putCasResponseAttributesIntoModel(final Map<String, Object> model,
                                                     final Map<String, Object> attributes,
                                                     final RegisteredService registeredService) {

        LOGGER.debug("Beginning to encode attributes for the response");
        val encodedAttributes = this.protocolAttributeEncoder.encodeAttributes(attributes, registeredService);

        LOGGER.debug("Encoded attributes for the response are [{}]", encodedAttributes);
        super.putIntoModel(model, CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES, encodedAttributes);

        val formattedAttributes = this.attributesRenderer.render(encodedAttributes);
        super.putIntoModel(model, CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FORMATTED_ATTRIBUTES, formattedAttributes);
    }
}
