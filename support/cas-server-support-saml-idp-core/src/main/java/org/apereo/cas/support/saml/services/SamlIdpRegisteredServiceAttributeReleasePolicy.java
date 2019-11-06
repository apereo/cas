package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SamlIdpRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Travis Schmidt
 * @since 6.2.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class SamlIdpRegisteredServiceAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 3020434998499030162L;

    /**
     * Gets attributes for saml registered service.
     *
     * @param attributes         the attributes
     * @param registeredService  the service
     * @param applicationContext the application context
     * @param resolver           the resolver
     * @param facade             the facade
     * @param entityDescriptor   the entity descriptor
     * @param principal          the principal
     * @param selectedService    the selected service
     * @return the attributes for saml registered service
     */
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(final Map<String, List<Object>> attributes,
                                                                              final SamlRegisteredService registeredService,
                                                                              final ApplicationContext applicationContext,
                                                                              final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                              final SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                              final EntityDescriptor entityDescriptor,
                                                                              final Principal principal,
                                                                              final Service selectedService) {
        try {
            val context = ApplicationContextProvider.getApplicationContext();
            val filter = (SamlIdpAttributeResolver) context.getAutowireCapableBeanFactory().getBean("samlIdpAttributeResolver");
            return filter.getAttributes(attributes, getAllowedAttributes());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>(0);
    }

}
