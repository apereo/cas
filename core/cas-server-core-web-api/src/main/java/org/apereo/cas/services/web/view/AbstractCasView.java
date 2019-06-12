package org.apereo.cas.services.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.servlet.view.AbstractView;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract class to handle retrieving the Assertion from the model.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractCasView extends AbstractView {

    /**
     * Indicate whether this view will be generating the success response or not.
     * By default, the view is treated as a failure.
     */
    protected final boolean successResponse;

    /**
     * The attribute encoder instance.
     */
    protected final ProtocolAttributeEncoder protocolAttributeEncoder;

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Authentication attribute release policy.
     */
    protected final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    /**
     * The Service selection strategy.
     */
    protected final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    /**
     * CAS attribute renderer.
     */
    protected final CasProtocolAttributesRenderer attributesRenderer;

    /**
     * Gets the assertion from the model.
     *
     * @param model the model
     * @return the assertion from
     */
    protected Assertion getAssertionFrom(final Map<String, Object> model) {
        return (Assertion) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION);
    }

    /**
     * Gets error code from.
     *
     * @param model the model
     * @return the error code from
     */
    protected String getErrorCodeFrom(final Map<String, Object> model) {
        return model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_CODE).toString();
    }

    /**
     * Gets error description from.
     *
     * @param model the model
     * @return the error description from
     */
    protected String getErrorDescriptionFrom(final Map<String, Object> model) {
        return model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_DESCRIPTION).toString();
    }


    /**
     * Gets the PGT-IOU from the model.
     *
     * @param model the model
     * @return the pgt-iou id
     */
    protected String getProxyGrantingTicketIou(final Map<String, Object> model) {
        return (String) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU);
    }

    /**
     * Gets the authentication from the model.
     *
     * @param model the model
     * @return the assertion from
     * @since 4.1.0
     */
    protected Authentication getPrimaryAuthenticationFrom(final Map<String, Object> model) {
        return getAssertionFrom(model).getPrimaryAuthentication();
    }

    /**
     * Gets model attributes.
     *
     * @param model the model
     * @return the model attributes
     */
    protected Map<String, Object> getModelAttributes(final Map<String, Object> model) {
        return (Map<String, Object>) model.get(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES);
    }


    /**
     * Gets the principal from the model.
     *
     * @param model the model
     * @return the assertion from
     * @since 4.1.0
     */
    protected Principal getPrincipal(final Map<String, Object> model) {
        return getPrimaryAuthenticationFrom(model).getPrincipal();
    }

    /**
     * Gets principal attributes.
     * Single-valued attributes are converted to a collection
     * so the review can easily loop through all.
     *
     * @param model the model
     * @return the attributes
     * @since 4.1.0
     */
    protected Map<String, List<Object>> getPrincipalAttributesAsMultiValuedAttributes(final Map<String, Object> model) {
        return getPrincipal(model).getAttributes();
    }


    /**
     * Gets validated service from the model.
     *
     * @param model the model
     * @return the validated service from
     */
    protected Service getServiceFrom(final Map<String, Object> model) {
        return (Service) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE);
    }

    /**
     * Gets chained authentications.
     * Note that the last index in the list always describes the primary authentication
     * event. All others in the chain should denote proxies. Per the CAS protocol,
     * when authentication has proceeded through multiple proxies,
     * the order in which the proxies were traversed MUST be reflected in the response.
     * The most recently-visited proxy MUST be the first proxy listed, and all the
     * other proxies MUST be shifted down as new proxies are added.
     *
     * @param model the model
     * @return the chained authentications
     */
    protected Collection<Authentication> getChainedAuthentications(final Map<String, Object> model) {
        val assertion = getAssertionFrom(model);
        val chainedAuthentications = assertion.getChainedAuthentications();
        return chainedAuthentications.stream().limit(chainedAuthentications.size() - 1).collect(Collectors.toList());
    }

    /**
     * Put into model.
     *
     * @param model the model
     * @param key   the key
     * @param value the value
     */
    protected void putIntoModel(final Map<String, Object> model, final String key, final Object value) {
        LOGGER.trace("Adding attribute [{}] into the view model for [{}] with value [{}]", key, getClass().getSimpleName(), value);
        model.put(key, value);
    }

    /**
     * Put cas authentication attributes into model.
     *
     * @param model             the model
     * @param registeredService the registered service
     * @return the cas authentication attributes
     */
    protected Map<String, List<Object>> getCasProtocolAuthenticationAttributes(final Map<String, Object> model,
                                                                         final RegisteredService registeredService) {
        val authn = getPrimaryAuthenticationFrom(model);
        val assertion = getAssertionFrom(model);

        return authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(authn, assertion, model, registeredService);
    }

    /**
     * Prepare view model with authentication principal.
     *
     * @param model the model
     * @return the map
     */
    protected Map prepareViewModelWithAuthenticationPrincipal(final Map<String, Object> model) {
        putIntoModel(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL, getPrincipal(model));
        putIntoModel(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_CHAINED_AUTHENTICATIONS, getChainedAuthentications(model));
        putIntoModel(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRIMARY_AUTHENTICATION, getPrimaryAuthenticationFrom(model));
        LOGGER.trace("Prepared CAS response output model with attribute names [{}]", model.keySet());
        return model;
    }

    /**
     * Prepare cas response attributes for view model.
     *
     * @param model the model
     */
    protected void prepareCasResponseAttributesForViewModel(final Map<String, Object> model) {
        val service = authenticationRequestServiceSelectionStrategies.resolveService(getServiceFrom(model));
        val registeredService = this.servicesManager.findServiceBy(service);

        val principalAttributes = getCasPrincipalAttributes(model, registeredService);
        val attributes = new HashMap<String, Object>(principalAttributes);

        LOGGER.trace("Processed principal attributes from the output model to be [{}]", principalAttributes.keySet());
        val protocolAttributes = getCasProtocolAuthenticationAttributes(model, registeredService);
        attributes.putAll(protocolAttributes);

        LOGGER.debug("Final collection of attributes for the response are [{}].", attributes.keySet());
        putCasResponseAttributesIntoModel(model, attributes, registeredService, this.attributesRenderer);
    }

    /**
     * Put cas principal attributes into model.
     *
     * @param model             the model
     * @param registeredService the registered service
     * @return the cas principal attributes
     */
    protected Map<String, List<Object>> getCasPrincipalAttributes(final Map<String, Object> model, final RegisteredService registeredService) {
        return getPrincipalAttributesAsMultiValuedAttributes(model);
    }

    /**
     * Put cas response attributes into model.
     *
     * @param model              the model
     * @param attributes         the attributes
     * @param registeredService  the registered service
     * @param attributesRenderer the attributes renderer
     */
    protected void putCasResponseAttributesIntoModel(final Map<String, Object> model,
                                                     final Map<String, Object> attributes,
                                                     final RegisteredService registeredService,
                                                     final CasProtocolAttributesRenderer attributesRenderer) {

        LOGGER.trace("Beginning to encode attributes for the response");
        val encodedAttributes = this.protocolAttributeEncoder.encodeAttributes(attributes, registeredService);

        LOGGER.debug("Encoded attributes for the response are [{}]", encodedAttributes);
        putIntoModel(model, CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES, encodedAttributes);

        val formattedAttributes = attributesRenderer.render(encodedAttributes);
        putIntoModel(model, CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FORMATTED_ATTRIBUTES, formattedAttributes);
    }
}
