package org.apereo.cas.support.saml.services.idp.metadata.plan;

import module java.base;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;

/**
 * This is {@link DefaultSamlRegisteredServiceMetadataResolutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
public class DefaultSamlRegisteredServiceMetadataResolutionPlan implements SamlRegisteredServiceMetadataResolutionPlan {
    private final Collection<SamlRegisteredServiceMetadataResolver> registeredMetadataResolvers = new ArrayList<>();

    @Override
    public void registerMetadataResolver(final SamlRegisteredServiceMetadataResolver clazz) {
        if (BeanSupplier.isNotProxy(clazz)) {
            registeredMetadataResolvers.add(clazz);
        }
    }
}
