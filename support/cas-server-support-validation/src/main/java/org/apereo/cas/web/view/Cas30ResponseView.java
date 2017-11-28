package org.apereo.cas.web.view;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renders and prepares CAS3 views. This view is responsible
 * to simply just prep the base model, and delegates to
 * a the real view to render the final output.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class Cas30ResponseView extends Cas20ResponseView {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cas30ResponseView.class);
    
    private final boolean releaseProtocolAttributes;

    public Cas30ResponseView(final boolean successResponse,
                             final ProtocolAttributeEncoder protocolAttributeEncoder,
                             final ServicesManager servicesManager,
                             final String authenticationContextAttribute,
                             final View view,
                             final boolean releaseProtocolAttributes,
                             final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                             final AuthenticationServiceSelectionPlan serviceSelectionStrategy) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authenticationContextAttribute, view,
                authenticationAttributeReleasePolicy, serviceSelectionStrategy);
        this.releaseProtocolAttributes = releaseProtocolAttributes;
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        super.prepareMergedOutputModel(model, request, response);

        final Service service = authenticationRequestServiceSelectionStrategies.resolveService(getServiceFrom(model));
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        final Map<String, Object> attributes = new HashMap<>();

        final Map<String, Object> principalAttributes = getCasPrincipalAttributes(model, registeredService);
        attributes.putAll(principalAttributes);

        LOGGER.debug("Processed response principal attributes from the output model to be [{}]", principalAttributes.keySet());
        if (this.releaseProtocolAttributes) {
            LOGGER.debug("CAS is configured to release protocol-level attributes. Processing...");
            final Map<String, Object> protocolAttributes = getCasProtocolAuthenticationAttributes(model, registeredService);
            attributes.putAll(protocolAttributes);
            LOGGER.debug("Processed response protocol/authentication attributes from the output model to be [{}]", protocolAttributes.keySet());
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

        // Authentication Attributes
        final Map<String, Object> filteredAuthenticationAttributes = authenticationAttributeReleasePolicy
                .getAuthenticationAttributesForRelease(getPrimaryAuthenticationFrom(model));

        // CAS 3.0 specific protocol attributes
        filteredAuthenticationAttributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
                CollectionUtils.wrap(getAuthenticationDate(model)));
        filteredAuthenticationAttributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN,
                CollectionUtils.wrap(isAssertionBackedByNewLogin(model)));
        filteredAuthenticationAttributes.put(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
                CollectionUtils.wrap(isRememberMeAuthentication(model)));

        final String contextProvider = getSatisfiedMultifactorAuthenticationProviderId(model);
        if (StringUtils.isNotBlank(contextProvider) && StringUtils.isNotBlank(authenticationContextAttribute)) {
            filteredAuthenticationAttributes.put(this.authenticationContextAttribute, CollectionUtils.wrap(contextProvider));
        }        

        return filteredAuthenticationAttributes;
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
        final Map<String, Object> encodedAttributes = this.protocolAttributeEncoder.encodeAttributes(attributes, registeredService);

        LOGGER.debug("Encoded attributes for the response are [{}]", encodedAttributes);
        super.putIntoModel(model, CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES, encodedAttributes);

        final List<String> formattedAttributes = new ArrayList<>(encodedAttributes.size());

        LOGGER.debug("Beginning to format/render attributes for the response");
        encodedAttributes.forEach((k, v) -> {
            final Set<Object> values = CollectionUtils.toCollection(v);
            values.forEach(value -> {
                final String fmt = new StringBuilder()
                        .append("<cas:".concat(k).concat(">"))
                        .append(StringEscapeUtils.escapeXml10(value.toString().trim()))
                        .append("</cas:".concat(k).concat(">"))
                        .toString();
                LOGGER.debug("Formatted attribute for the response: [{}]", fmt);
                formattedAttributes.add(fmt);
            });
        });
        super.putIntoModel(model, CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FORMATTED_ATTRIBUTES, formattedAttributes);
    }
}
