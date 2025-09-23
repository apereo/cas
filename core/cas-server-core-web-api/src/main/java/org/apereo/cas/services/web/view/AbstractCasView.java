package org.apereo.cas.services.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.view.AbstractView;
import java.util.ArrayList;
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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
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

    protected final AttributeDefinitionStore attributeDefinitionStore;

    protected Assertion getAssertionFrom(final Map<String, Object> model) {
        return (Assertion) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION);
    }

    protected String getErrorCodeFrom(final Map<String, Object> model) {
        return model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_CODE).toString();
    }

    protected String getErrorDescriptionFrom(final Map<String, Object> model) {
        return model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_DESCRIPTION).toString();
    }


    protected String getProxyGrantingTicketIou(final Map<String, Object> model) {
        return (String) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU);
    }

    protected Authentication getPrimaryAuthenticationFrom(final Map<String, Object> model) {
        return getAssertionFrom(model).getPrimaryAuthentication();
    }

    protected Map<String, Object> getModelAttributes(final Map<String, Object> model) {
        return (Map<String, Object>) model.get(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES);
    }

    protected Principal getPrincipal(final Map<String, Object> model) {
        return getPrimaryAuthenticationFrom(model).getPrincipal();
    }

    protected Map<String, List<Object>> getPrincipalAttributesAsMultiValuedAttributes(final Map<String, Object> model) {
        return getPrincipal(model).getAttributes();
    }

    protected WebApplicationService getServiceFrom(final Map<String, Object> model) {
        return (WebApplicationService) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE);
    }

    protected List<Authentication> getChainedAuthentications(final Map<String, Object> model) {
        val assertion = getAssertionFrom(model);
        val chainedAuthentications = assertion.getChainedAuthentications();
        return chainedAuthentications.stream().limit(chainedAuthentications.size() - 1).collect(Collectors.toList());
    }

    protected void putIntoModel(final Map<String, Object> model, final String key, final Object value) {
        LOGGER.trace("Adding attribute [{}] into the view model for [{}] with value [{}]", key, getClass().getSimpleName(), value);
        model.put(key, value);
    }

    protected Map<String, List<Object>> getCasProtocolAuthenticationAttributes(final Map<String, Object> model,
                                                                               final RegisteredService registeredService) {
        val authn = getPrimaryAuthenticationFrom(model);
        val assertion = getAssertionFrom(model);
        return authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(authn, assertion, model, registeredService);
    }

    protected Map prepareViewModelWithAuthenticationPrincipal(final Map<String, Object> model) {
        putIntoModel(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL, getPrincipal(model));
        val chain = getChainedAuthentications(model);
        if (!chain.isEmpty()) {
            val listOfProxies = new ArrayList<String>();
            chain.forEach(authentication ->
                authentication.getCredentials()
                    .stream()
                    .filter(HttpBasedServiceCredential.class::isInstance)
                    .map(HttpBasedServiceCredential.class::cast)
                    .map(credential -> credential.getCredentialMetadata()
                        .getProperty(HttpBasedServiceCredential.class.getName(), String.class))
                    .filter(StringUtils::isNotBlank)
                    .forEach(listOfProxies::add));
            if (!listOfProxies.isEmpty()) {
                putIntoModel(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXIES, listOfProxies);
            }
        }
        putIntoModel(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRIMARY_AUTHENTICATION, getPrimaryAuthenticationFrom(model));
        LOGGER.trace("Prepared CAS response output model with attribute names [{}]", model.keySet());
        return model;
    }

    protected void prepareCasResponseAttributesForViewModel(final Map<String, Object> model) {
        FunctionUtils.doUnchecked(__ -> {
            val service = authenticationRequestServiceSelectionStrategies.resolveService(getServiceFrom(model));
            val registeredService = servicesManager.findServiceBy(service);

            val principalAttributes = getCasPrincipalAttributes(model, registeredService);
            val attributes = new HashMap<String, Object>(principalAttributes);

            LOGGER.trace("Processed principal attributes from the output model to be [{}]", principalAttributes.keySet());
            val protocolAttributes = getCasProtocolAuthenticationAttributes(model, registeredService);
            attributes.putAll(protocolAttributes);

            LOGGER.debug("Final collection of attributes for the response are [{}].", attributes.keySet());
            putCasResponseAttributesIntoModel(model, attributes, registeredService, attributesRenderer);
        });
    }

    protected Map<String, List<Object>> getCasPrincipalAttributes(final Map<String, Object> model, final RegisteredService registeredService) {
        return getPrincipalAttributesAsMultiValuedAttributes(model);
    }

    protected void putCasResponseAttributesIntoModel(final Map<String, Object> model,
                                                     final Map<String, Object> attributes,
                                                     final RegisteredService registeredService,
                                                     final CasProtocolAttributesRenderer attributesRenderer) {

        LOGGER.trace("Beginning to encode attributes for the response");
        val webApplicationService = getServiceFrom(model);
        val encodedAttributes = protocolAttributeEncoder.encodeAttributes(model, attributes, registeredService, webApplicationService);

        LOGGER.debug("Encoded attributes for the response are [{}]", encodedAttributes);
        putIntoModel(model, CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES, encodedAttributes);

        val formattedAttributes = attributesRenderer.render(encodedAttributes);
        if (!formattedAttributes.isEmpty()) {
            putIntoModel(model, CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FORMATTED_ATTRIBUTES, formattedAttributes);
        }
    }
}
