package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.BaseServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.core.Ordered;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link SamlIdPServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class SamlIdPServicesManagerRegisteredServiceLocator extends BaseServicesManagerRegisteredServiceLocator {

    public SamlIdPServicesManagerRegisteredServiceLocator(
        final SamlRegisteredServiceCachingMetadataResolver resolver) {

        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, service) -> {
            val parameterValue = getSamlParameterValue(registeredService, service);
            return parameterValue
                .map(pair -> {
                    val attribute = pair.getLeft();
                    val attributeValue = pair.getRight();
                    LOGGER.trace("Located service attribute [{}] with value [{}]", attribute, attributeValue);
                    return attribute.getEntityIdFrom(resolver, attributeValue);
                })
                .filter(StringUtils::isNotBlank)
                .filter(StringUtils::isNotBlank)
                .filter(registeredService::matches)
                .stream()
                .anyMatch(entityId -> {
                    LOGGER.trace("Resolving metadata for service [{}] via entity id [{}]", registeredService.getName(), entityId);
                    val samlService = (SamlRegisteredService) registeredService;
                    val adaptor = SamlRegisteredServiceMetadataAdaptor.get(resolver, samlService, entityId);
                    return adaptor.isPresent();
                });
        });
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        val matched = SamlRegisteredService.class.isAssignableFrom(registeredService.getClass())
            && registeredService.getFriendlyName().equalsIgnoreCase(SamlRegisteredService.FRIENDLY_NAME);
        return matched && getSamlParameterValue(registeredService, service).isPresent();
    }

    /**
     * Get the saml2 request or entity id as a service attribute.
     *
     * @param registeredService the registered service
     * @param service           the service
     * @return true/false
     */
    protected Optional<Pair<SamlProtocolServiceAttribute, String>> getSamlParameterValue(final RegisteredService registeredService,
                                                                                         final Service service) {
        if (registeredService instanceof SamlRegisteredService) {
            val attributes = service.getAttributes();
            LOGGER.trace("Reviewing service attributes [{}] for service id [{}] to match registered service [{}]",
                attributes, service.getId(), registeredService.getName());

            return SamlProtocolServiceAttribute.values()
                .stream()
                .sorted(Comparator.comparing(SamlProtocolServiceAttribute::getOrder))
                .filter(attr -> attributes.containsKey(attr.getAttributeName()))
                .map(attr -> Pair.of(attr, CollectionUtils.firstElement(attributes.get(attr.getAttributeName()))
                    .map(Object::toString).orElseThrow()))
                .findFirst();
        }
        LOGGER.trace("Registered service [{}] is not a SAML2 registered service", registeredService.getName());
        return Optional.empty();
    }

    @Override
    protected Pair<String, Class<? extends RegisteredService>> getRegisteredServiceIndexedType() {
        return Pair.of(SamlRegisteredService.FRIENDLY_NAME, SamlRegisteredService.class);
    }

    @RequiredArgsConstructor
    @Getter
    private static class SamlProtocolServiceAttribute implements Ordered {
        private static final SamlProtocolServiceAttribute SAML_REQUEST =
            new SamlProtocolServiceAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, Ordered.LOWEST_PRECEDENCE) {
                @Override
                public String getEntityIdFrom(final SamlRegisteredServiceCachingMetadataResolver resolver, final String attributeValue) {
                    val openSamlConfigBean = resolver.getOpenSamlConfigBean();
                    val sessionStore = openSamlConfigBean.getApplicationContext()
                        .getBean("samlIdPDistributedSessionStore", SessionStore.class);
                    val authnRequest = SamlIdPSessionManager.of(resolver.getOpenSamlConfigBean(), sessionStore)
                        .fetch(RequestAbstractType.class, attributeValue);
                    openSamlConfigBean.logObject(authnRequest);
                    return SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
                }
            };

        private static final SamlProtocolServiceAttribute ENTITY_ID =
            new SamlProtocolServiceAttribute(SamlProtocolConstants.PARAMETER_ENTITY_ID, Ordered.HIGHEST_PRECEDENCE);

        private static final SamlProtocolServiceAttribute PROVIDER_ID =
            new SamlProtocolServiceAttribute(SamlIdPConstants.PROVIDER_ID, Ordered.HIGHEST_PRECEDENCE);

        private final String attributeName;

        private final int order;

        /**
         * Known values.
         *
         * @return the list
         */
        static List<SamlProtocolServiceAttribute> values() {
            return List.of(ENTITY_ID, PROVIDER_ID, SAML_REQUEST);
        }

        /**
         * Gets entity id from value.
         *
         * @param resolver       the resolver
         * @param attributeValue the attribute value
         * @return the entity id from
         */
        @SuppressWarnings("UnusedVariable")
        String getEntityIdFrom(final SamlRegisteredServiceCachingMetadataResolver resolver, final String attributeValue) {
            return attributeValue;
        }
    }
}
