package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.Ordered;

import java.util.Optional;

/**
 * This is {@link SamlIdPMetadataCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface SamlIdPMetadataCustomizer extends Ordered {
    /**
     * Customize the generated idp metadata.
     *
     * @param entityDescriptor  the entity descriptor
     * @param registeredService the registered service
     */
    void customize(EntityDescriptor entityDescriptor, Optional<SamlRegisteredService> registeredService);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
