package org.apereo.cas.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private final boolean releaseProtocolAttributes;

    public Cas30ResponseView(final boolean successResponse,
                             final ProtocolAttributeEncoder protocolAttributeEncoder,
                             final ServicesManager servicesManager,
                             final String authenticationContextAttribute,
                             final View view,
                             final boolean releaseProtocolAttributes,
                             final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                             final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                             final CasProtocolAttributesRenderer attributesRenderer) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authenticationContextAttribute, view,
            authenticationAttributeReleasePolicy, serviceSelectionStrategy);
        this.releaseProtocolAttributes = releaseProtocolAttributes;
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
        if (this.releaseProtocolAttributes) {
            LOGGER.debug("CAS is configured to release protocol-level attributes. Processing...");
            val protocolAttributes = getCasProtocolAuthenticationAttributes(model, registeredService);
            attributes.putAll(protocolAttributes);
            LOGGER.debug("Processed protocol/authentication attributes from the output model to be [{}]", protocolAttributes.keySet());
        }

        decideIfCredentialPasswordShouldBeReleasedAsAttribute(attributes, model, registeredService);
        decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(attributes, model, registeredService);

        LOGGER.debug("Final collection of attributes for the response are [{}].", attributes.keySet());
        putCasResponseAttributesIntoModel(model, attributes, registeredService);
    }

    /**
     * Put cas authentication attributes into model.
     *
     * @param model             the model
     * @param registeredService the registered service
     * @return the cas authentication attributes
     */
    protected Map<String, Object> getCasProtocolAuthenticationAttributes(final Map<String, Object> model,
                                                                         final RegisteredService registeredService) {

        if (!registeredService.getAttributeReleasePolicy().isAuthorizedToReleaseAuthenticationAttributes()) {
            LOGGER.debug("Attribute release policy for service [{}] is configured to never release any attributes", registeredService);
            return new LinkedHashMap<>(0);
        }

        val filteredAuthenticationAttributes = authenticationAttributeReleasePolicy
            .getAuthenticationAttributesForRelease(getPrimaryAuthenticationFrom(model));

        filterCasProtocolAttributes(model, filteredAuthenticationAttributes);

        val contextProvider = getSatisfiedMultifactorAuthenticationProviderId(model);
        if (StringUtils.isNotBlank(contextProvider) && StringUtils.isNotBlank(authenticationContextAttribute)) {
            filteredAuthenticationAttributes.put(this.authenticationContextAttribute, CollectionUtils.wrap(contextProvider));
        }

        return filteredAuthenticationAttributes;
    }

    private void filterCasProtocolAttributes(final Map<String, Object> model, final Map<String, Object> filteredAuthenticationAttributes) {
        filteredAuthenticationAttributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
            CollectionUtils.wrap(getAuthenticationDate(model)));
        filteredAuthenticationAttributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN,
            CollectionUtils.wrap(isAssertionBackedByNewLogin(model)));
        filteredAuthenticationAttributes.put(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
            CollectionUtils.wrap(isRememberMeAuthentication(model)));
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
