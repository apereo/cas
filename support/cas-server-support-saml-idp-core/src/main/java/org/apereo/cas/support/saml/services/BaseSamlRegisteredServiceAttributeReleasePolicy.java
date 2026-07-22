package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

/**
 * This is {@link BaseSamlRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public abstract class BaseSamlRegisteredServiceAttributeReleasePolicy extends ReturnAllowedAttributeReleasePolicy {
    @Serial
    private static final long serialVersionUID = -3301632236702329694L;
    
    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> attributes) {
        if (context.getRegisteredService() instanceof SamlRegisteredService) {
            val applicationContext = context.getApplicationContext();
            val resolver = applicationContext.getBean(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME,
                SamlRegisteredServiceCachingMetadataResolver.class);
            val entityId = SamlIdPSAttributeReleasePolicyUtils.getEntityIdFromRequest(context);
            val facade = StringUtils.isBlank(entityId)
                ? Optional.<SamlRegisteredServiceMetadataAdaptor>empty()
                : SamlIdPSAttributeReleasePolicyUtils.determineServiceProviderMetadataFacade(context, entityId);

            if (facade.isEmpty()) {
                LOGGER.warn("Could not locate metadata for [{}] to process attributes", entityId);
                return new HashMap<>();
            }

            val entityDescriptor = facade.get().getEntityDescriptor();
            return getAttributesForSamlRegisteredService(attributes, resolver, facade.get(), entityDescriptor, context);
        }
        return authorizeReleaseOfAllowedAttributes(context, attributes);
    }

    protected abstract Map<String, List<Object>> getAttributesForSamlRegisteredService(
        Map<String, List<Object>> attributes,
        SamlRegisteredServiceCachingMetadataResolver resolver,
        SamlRegisteredServiceMetadataAdaptor facade,
        EntityDescriptor entityDescriptor,
        RegisteredServiceAttributeReleasePolicyContext context);
}
