package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Optional;

/**
 * This is {@link SamlIdPServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class SamlIdPServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    public SamlIdPServicesManagerRegisteredServiceLocator(final SamlRegisteredServiceCachingMetadataResolver resolver) {
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
                .filter(registeredService::matches)
                .stream()
                .anyMatch(entityId -> {
                    LOGGER.trace("Resolving metadata for service [{}] via entity id [{}]", registeredService.getName(), entityId);
                    val samlService = SamlRegisteredService.class.cast(registeredService);
                    val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlService, entityId);
                    return adaptor.isPresent();
                });
        });
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        return getSamlParameterValue(registeredService, service).isPresent();
    }

    /**
     * Get the saml2 request or entity id as a service atribute.
     *
     * @param registeredService the registered service
     * @param service           the service
     * @return the boolean
     */
    protected Optional<Pair<SamlProtocolServiceAttribute, String>> getSamlParameterValue(final RegisteredService registeredService,
                                                                                         final Service service) {
        if (registeredService instanceof SamlRegisteredService) {
            val attributes = service.getAttributes();
            LOGGER.trace("Reviewing service attributes [{}] for service id [{}] to match registered service [{}]",
                attributes, service.getId(), registeredService.getName());

            return SamlProtocolServiceAttribute.values()
                .stream()
                .filter(attr -> attributes.containsKey(attr.getAttributeName()))
                .map(attr -> Pair.of(attr, CollectionUtils.firstElement(attributes.get(attr.getAttributeName()))
                    .map(Object::toString).orElseThrow()))
                .findFirst();
        }
        LOGGER.trace("Registered service [{}] is not a SAML2 registered service", registeredService.getName());
        return Optional.empty();
    }

    @RequiredArgsConstructor
    @Getter
    private static class SamlProtocolServiceAttribute {
        private static final SamlProtocolServiceAttribute SAML_REQUEST = new SamlProtocolServiceAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST) {
            @Override
            public String getEntityIdFrom(final SamlRegisteredServiceCachingMetadataResolver resolver, final String attributeValue) {
                val openSamlConfigBean = resolver.getOpenSamlConfigBean();
                val authnRequest = SamlIdPUtils.retrieveSamlRequest(openSamlConfigBean, RequestAbstractType.class, attributeValue);
                SamlUtils.logSamlObject(openSamlConfigBean, authnRequest);
                return SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
            }
        };

        private static final SamlProtocolServiceAttribute ENTITY_ID = new SamlProtocolServiceAttribute(SamlProtocolConstants.PARAMETER_ENTITY_ID);

        private static final SamlProtocolServiceAttribute PROVIDER_ID = new SamlProtocolServiceAttribute(SamlIdPConstants.PROVIDER_ID);

        private final String attributeName;

        /**
         * Known values.
         *
         * @return the list
         */
        public static List<SamlProtocolServiceAttribute> values() {
            return List.of(SAML_REQUEST, ENTITY_ID, PROVIDER_ID);
        }

        /**
         * Gets entity id from value.
         *
         * @param resolver       the resolver
         * @param attributeValue the attribute value
         * @return the entity id from
         */
        public String getEntityIdFrom(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                      final String attributeValue) {
            return attributeValue;
        }
    }
}
