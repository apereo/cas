package org.apereo.cas.support.saml.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.StatusResponseType;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.core.Ordered;

/**
 * This is {@link SamlIdPServiceAttributeExtractor}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class SamlIdPServiceAttributeExtractor {

    private static final SamlIdPServiceAttributeExtractor SAML_REQUEST =
        new SamlIdPServiceAttributeExtractor(SamlProtocolConstants.PARAMETER_SAML_REQUEST, Ordered.LOWEST_PRECEDENCE) {
            @Override
            public @Nullable String getEntityIdFrom(final SamlRegisteredServiceCachingMetadataResolver resolver, final String attributeValue) {
                val openSamlConfigBean = resolver.getOpenSamlConfigBean();
                val sessionStore = openSamlConfigBean.getApplicationContext()
                    .getBean("samlIdPDistributedSessionStore", SessionStore.class);
                val authnRequest = SamlIdPSessionManager.of(resolver.getOpenSamlConfigBean(), sessionStore)
                    .fetch(RequestAbstractType.class, attributeValue);
                openSamlConfigBean.logObject(authnRequest);
                return SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
            }
        };

    private static final SamlIdPServiceAttributeExtractor ENTITY_ID =
        new SamlIdPServiceAttributeExtractor(SamlProtocolConstants.PARAMETER_ENTITY_ID, Ordered.HIGHEST_PRECEDENCE);

    private static final SamlIdPServiceAttributeExtractor PROVIDER_ID =
        new SamlIdPServiceAttributeExtractor(SamlIdPConstants.PROVIDER_ID, Ordered.HIGHEST_PRECEDENCE);

    private static final SamlIdPServiceAttributeExtractor SAML_RESPONSE_ENCODED =
        new SamlIdPServiceAttributeExtractor(SamlProtocolConstants.PARAMETER_SAML_RESPONSE, Ordered.LOWEST_PRECEDENCE) {
            @Override
            public @Nullable String getEntityIdFrom(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                    final String attributeValue) {
                return extractEntityFromEncodedSamlObject(resolver, attributeValue, StatusResponseType.class);
            }
        };

    private static final SamlIdPServiceAttributeExtractor SAML_REQUEST_ENCODED =
        new SamlIdPServiceAttributeExtractor(SamlProtocolConstants.PARAMETER_SAML_REQUEST, Ordered.LOWEST_PRECEDENCE) {
            @Override
            public @Nullable String getEntityIdFrom(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                    final String attributeValue) {
                return extractEntityFromEncodedSamlObject(resolver, attributeValue, RequestAbstractType.class);
            }
        };
    
    private final String attributeName;

    private final int order;

    /**
     * Known values.
     *
     * @return the list
     */
    static List<SamlIdPServiceAttributeExtractor> values() {
        return List.of(
            ENTITY_ID,
            PROVIDER_ID,
            SAML_REQUEST,
            SAML_REQUEST_ENCODED,
            SAML_RESPONSE_ENCODED
        );
    }

    /**
     * Gets entity id from value.
     *
     * @param resolver       the resolver
     * @param attributeValue the attribute value
     * @return the entity id from
     */
    @SuppressWarnings("UnusedVariable")
    public @Nullable String getEntityIdFrom(final SamlRegisteredServiceCachingMetadataResolver resolver, final String attributeValue) {
        return attributeValue;
    }
    
    private static @Nullable String extractEntityFromEncodedSamlObject(
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final String attributeValue,
        final Class clazz) {
        val openSamlConfigBean = resolver.getOpenSamlConfigBean();
        val response = SamlUtils.convertToSamlObject(openSamlConfigBean, attributeValue, clazz);
        if (response != null) {
            openSamlConfigBean.logObject(response);
            return SamlIdPUtils.getIssuerFromSamlObject(response);
        }
        return null;
    }

    /**
     * Get the saml2 request or entity id as a service attribute.
     *
     * @param registeredService the registered service
     * @param service           the service
     * @return true/false
     */
    public static Optional<Pair<SamlIdPServiceAttributeExtractor, String>> extract(
        final RegisteredService registeredService,
        final Service service) {
        if (registeredService instanceof SamlRegisteredService) {
            val attributes = service.getAttributes();
            LOGGER.trace("Reviewing service attributes [{}] for service id [{}] to match registered service [{}]",
                attributes, service.getId(), registeredService.getName());

            return SamlIdPServiceAttributeExtractor
                .values()
                .stream()
                .sorted(Comparator.comparing(SamlIdPServiceAttributeExtractor::getOrder))
                .filter(attr -> attributes.containsKey(attr.getAttributeName()))
                .map(attr -> Pair.of(attr, CollectionUtils.firstElement(attributes.get(attr.getAttributeName()))
                    .map(Object::toString).orElseThrow()))
                .findFirst();
        }
        LOGGER.trace("Registered service [{}] is not a SAML2 registered service", registeredService.getName());
        return Optional.empty();
    }
}
