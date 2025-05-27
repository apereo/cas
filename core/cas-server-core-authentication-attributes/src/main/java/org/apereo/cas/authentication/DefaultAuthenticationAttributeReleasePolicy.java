package org.apereo.cas.authentication;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;

import lombok.Getter;
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
@Getter
public class DefaultAuthenticationAttributeReleasePolicy implements AuthenticationAttributeReleasePolicy {

    private final Collection<String> onlyReleaseAttributes;

    private final Collection<String> neverReleaseAttributes;

    private final String authenticationContextAttribute;

    public DefaultAuthenticationAttributeReleasePolicy(final String authenticationContextAttribute) {
        this(new ArrayList<>(), new ArrayList<>(), authenticationContextAttribute);
    }

    @Override
    public Map<String, List<Object>> getAuthenticationAttributesForRelease(final Authentication authentication,
                                                                           final Assertion assertion,
                                                                           final Map<String, Object> model,
                                                                           final RegisteredService service) {
        if (!service.getAttributeReleasePolicy().isAuthorizedToReleaseAuthenticationAttributes()) {
            LOGGER.debug("Attribute release policy for service [{}] is configured to never release any authentication attributes", service.getServiceId());
            return new LinkedHashMap<>();
        }
        val attrs = getAuthenticationAttributesForRelease(authentication, service);

        if (isAttributeAllowedForRelease(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN)) {
            var forceAuthn = assertion != null && assertion.isFromNewLogin();
            if (!forceAuthn) {
                val values = authentication.getAttributes().getOrDefault(
                    CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN, List.of(Boolean.FALSE));
                forceAuthn = values.contains(Boolean.TRUE);
            }
            attrs.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN, CollectionUtils.wrap(forceAuthn));
        }

        if (isAttributeAllowedForRelease(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME)) {
            var rememberMe = assertion != null && CoreAuthenticationUtils.isRememberMeAuthentication(authentication, assertion);
            if (!rememberMe) {
                val values = authentication.getAttributes().getOrDefault(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME, List.of(Boolean.FALSE));
                rememberMe = values.contains(Boolean.TRUE);
            }
            attrs.put(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME, CollectionUtils.wrap(rememberMe));
        }

        if (StringUtils.isNotBlank(authenticationContextAttribute)) {
            org.springframework.util.StringUtils.commaDelimitedListToSet(authenticationContextAttribute)
                .stream()
                .filter(model::containsKey)
                .forEach(attr -> {
                    val contextProvider = CollectionUtils.firstElement(model.get(attr));
                    contextProvider.ifPresent(provider -> {
                        if (isAttributeAllowedForRelease(attr)) {
                            attrs.put(attr, CollectionUtils.wrap(provider));
                        }
                    });
                });
        }
        decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(attrs, model, service);
        LOGGER.trace("Processed protocol/authentication attributes from the output model to be [{}]", attrs.keySet());
        return attrs;
    }

    @Override
    public Map<String, List<Object>> getAuthenticationAttributesForRelease(final Authentication authentication,
                                                                           final RegisteredService service) {
        if (service == null || !service.getAttributeReleasePolicy().isAuthorizedToReleaseAuthenticationAttributes()) {
            LOGGER.debug("Attribute release policy for service [{}] is configured to never release any authentication attributes", service);
            return new LinkedHashMap<>();
        }

        val attrs = new LinkedHashMap<>(authentication.getAttributes());
        attrs.keySet().removeAll(neverReleaseAttributes);

        if (onlyReleaseAttributes != null && !onlyReleaseAttributes.isEmpty()) {
            attrs.keySet().retainAll(onlyReleaseAttributes);
        }

        if (isAttributeAllowedForRelease(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE)) {
            attrs.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
                CollectionUtils.wrap(authentication.getAuthenticationDate()));
        }

        decideIfCredentialPasswordShouldBeReleasedAsAttribute(attrs, authentication, service);

        LOGGER.trace("Processed protocol/authentication attributes from the output model to be [{}]", attrs.keySet());
        return attrs;
    }

    protected boolean isAttributeAllowedForRelease(final String attributeName) {
        return !neverReleaseAttributes.contains(attributeName)
               && (onlyReleaseAttributes.isEmpty() || onlyReleaseAttributes.contains(attributeName));
    }

    protected void decideIfCredentialPasswordShouldBeReleasedAsAttribute(final Map<String, List<Object>> attributes,
                                                                         final Authentication authentication,
                                                                         final RegisteredService service) {
        val policy = service.getAttributeReleasePolicy();
        val isAuthorized = policy != null && policy.isAuthorizedToReleaseCredentialPassword();
        val element = CollectionUtils.firstElement(authentication.getAttributes().get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL));
        val credential = element.map(Object::toString).orElse(null);
        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes, credential,
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, service, isAuthorized);
    }

    protected void decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(final Map<String, List<Object>> attributes,
                                                                          final Map<String, Object> model,
                                                                          final RegisteredService service) {
        val policy = service.getAttributeReleasePolicy();
        val isAuthorized = policy != null && policy.isAuthorizedToReleaseProxyGrantingTicket();

        val pgtIou = (String) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU);
        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes, pgtIou,
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU, service, isAuthorized);
        val proxyGrantingTicketId = model.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET)
            ? ((Ticket) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET)).getId()
            : StringUtils.EMPTY;
        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes, proxyGrantingTicketId,
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, service, isAuthorized);
    }

    protected void decideAttributeReleaseBasedOnServiceAttributePolicy(final Map<String, List<Object>> attributes,
                                                                       final String attributeValue,
                                                                       final String attributeName,
                                                                       final RegisteredService service,
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
            attributes.remove(attributeName);
        }
    }
}
