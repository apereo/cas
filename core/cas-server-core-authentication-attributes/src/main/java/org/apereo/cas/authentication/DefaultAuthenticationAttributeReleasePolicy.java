package org.apereo.cas.authentication;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default AuthenticationAttributeReleasePolicy implementation.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultAuthenticationAttributeReleasePolicy implements AuthenticationAttributeReleasePolicy {

    private final Collection<String> onlyReleaseAttributes;
    private final Collection<String> neverReleaseAttributes;
    private final String authenticationContextAttribute;

    public DefaultAuthenticationAttributeReleasePolicy(final String authenticationContextAttribute) {
        this(new ArrayList<>(0), new ArrayList<>(0), authenticationContextAttribute);
    }


    @Override
    public Map<String, List<Object>> getAuthenticationAttributesForRelease(final Authentication authentication,
                                                                           final Assertion assertion,
                                                                           final Map<String, Object> model,
                                                                           final RegisteredService service) {

        if (!service.getAttributeReleasePolicy().isAuthorizedToReleaseAuthenticationAttributes()) {
            LOGGER.debug("Attribute release policy for service [{}] is configured to never release any attributes", service);
            return new LinkedHashMap<>(0);
        }

        val attrs = new LinkedHashMap<String, List<Object>>(authentication.getAttributes());
        attrs.keySet().removeAll(neverReleaseAttributes);

        if (onlyReleaseAttributes != null && !onlyReleaseAttributes.isEmpty()) {
            attrs.keySet().retainAll(onlyReleaseAttributes);
        }

        if (isAttributeAllowedForRelease(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE)) {
            attrs.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE, CollectionUtils.wrap(authentication.getAuthenticationDate()));
        }

        if (assertion != null) {
            if (isAttributeAllowedForRelease(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN)) {
                attrs.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN, CollectionUtils.wrap(assertion.isFromNewLogin()));
            }
            if (isAttributeAllowedForRelease(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME)) {
                attrs.put(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
                    CollectionUtils.wrap(CoreAuthenticationUtils.isRememberMeAuthentication(authentication, assertion)));
            }
        }

        if (StringUtils.isNotBlank(authenticationContextAttribute) && model.containsKey(this.authenticationContextAttribute)) {
            val contextProvider = model.get(this.authenticationContextAttribute).toString();
            if (StringUtils.isNotBlank(contextProvider) && isAttributeAllowedForRelease(authenticationContextAttribute)) {
                attrs.put(this.authenticationContextAttribute, CollectionUtils.wrap(contextProvider));
            }
        }

        decideIfCredentialPasswordShouldBeReleasedAsAttribute(attrs, authentication, service);
        decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(attrs, model, service);
        LOGGER.trace("Processed protocol/authentication attributes from the output model to be [{}]", attrs.keySet());
        return attrs;
    }

    /**
     * Is attribute allowed for release?
     *
     * @param attributeName the attribute name
     * @return true/false
     */
    protected boolean isAttributeAllowedForRelease(final String attributeName) {
        return !this.neverReleaseAttributes.contains(attributeName);
    }

    /**
     * Decide if credential password should be released as attribute.
     * The credential must have been cached as an authentication attribute
     * and the attribute release policy must be allowed to release the
     * attribute.
     *
     * @param attributes     the attributes
     * @param authentication the authentication
     * @param service        the service
     */
    protected void decideIfCredentialPasswordShouldBeReleasedAsAttribute(final Map<String, List<Object>> attributes, final Authentication authentication,
                                                                         final RegisteredService service) {
        val policy = service.getAttributeReleasePolicy();
        val isAuthorized = policy != null && policy.isAuthorizedToReleaseCredentialPassword();

        val element = CollectionUtils.firstElement(authentication.getAttributes().get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL));
        val credential = element.map(Object::toString).orElse(null);
        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes, credential,
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, service, isAuthorized);
    }

    /**
     * Decide if PGT should be released as attribute.
     * The PGT must have been cached as an authentication attribute
     * and the attribute release policy must be allowed to release the
     * attribute.
     *
     * @param attributes the attributes
     * @param model      the model
     * @param service    the service
     */
    protected void decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(final Map<String, List<Object>> attributes,
                                                                          final Map<String, Object> model, final RegisteredService service) {
        val policy = service.getAttributeReleasePolicy();
        val isAuthorized = policy != null && policy.isAuthorizedToReleaseProxyGrantingTicket();

        val pgtIou = (String) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU);
        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes, pgtIou,
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU, service, isAuthorized);
        val pgtId = (String) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes, pgtId,
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, service, isAuthorized);
    }

    /**
     * Decide attribute release based on service attribute policy.
     *
     * @param attributes               the attributes
     * @param attributeValue           the attribute value
     * @param attributeName            the attribute name
     * @param service                  the service
     * @param doesAttributePolicyAllow does attribute policy allow release of this attribute?
     */
    protected void decideAttributeReleaseBasedOnServiceAttributePolicy(final Map<String, List<Object>> attributes, final String attributeValue,
                                                                       final String attributeName, final RegisteredService service,
                                                                       final boolean doesAttributePolicyAllow) {
        if (StringUtils.isNotBlank(attributeValue)) {
            LOGGER.debug("Obtained [{}] as an authentication attribute", attributeName);
            if (doesAttributePolicyAllow) {
                LOGGER.debug("Obtained [{}] is passed to the CAS validation payload", attributeName);
                attributes.put(attributeName, CollectionUtils.wrap(attributeValue));
            } else {
                LOGGER.debug("Attribute release policy for [{}] does not authorize the release of [{}]", service.getServiceId(), attributeName);
                attributes.remove(attributeName);
            }
        } else {
            LOGGER.trace("[{}] is not available and will not be released to the validation response.", attributeName);
        }
    }
}
