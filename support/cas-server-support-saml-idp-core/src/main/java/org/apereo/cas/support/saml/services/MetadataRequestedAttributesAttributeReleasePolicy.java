package org.apereo.cas.support.saml.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link MetadataRequestedAttributesAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MetadataRequestedAttributesAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataRequestedAttributesAttributeReleasePolicy.class);
    private static final long serialVersionUID = -3483733307124962357L;

    private boolean useFriendlyName;

    @Override
    protected Map<String, Object> getAttributesForSamlRegisteredService(final Map<String, Object> attributes, final SamlRegisteredService service,
                                                                        final ApplicationContext applicationContext,
                                                                        final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                        final SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                        final EntityDescriptor entityDescriptor) {

        final Map<String, Object> releaseAttributes = new LinkedHashMap<>();
        final SPSSODescriptor sso = facade.getSsoDescriptor();
        if (sso != null) {
            sso.getAttributeConsumingServices().forEach(svc ->
                    svc.getRequestAttributes()
                            .stream()
                            .filter(attr -> {
                                final String name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                                LOGGER.debug("Checking for requested attribute [{}] in metadata for [{}]", name, service.getName());
                                return attributes.containsKey(name);
                            })
                            .forEach(attr -> {
                                final String name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                                LOGGER.debug("Found requested attribute [{}] in metadata for [{}]", name, service.getName());
                                releaseAttributes.put(name, attributes.get(name));
                            }));
        }
        return releaseAttributes;
    }

    public boolean isUseFriendlyName() {
        return useFriendlyName;
    }

    public void setUseFriendlyName(final boolean useFriendlyName) {
        this.useFriendlyName = useFriendlyName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("useFriendlyName", useFriendlyName)
                .toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final MetadataRequestedAttributesAttributeReleasePolicy rhs = (MetadataRequestedAttributesAttributeReleasePolicy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.useFriendlyName, rhs.useFriendlyName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(useFriendlyName)
                .toHashCode();
    }
}
