package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.RegexUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link MetadataEntityGroupAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MetadataEntityGroupAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -5274638817124962357L;

    private String group;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceMetadataAdaptor facade,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {

        val affiliationDescriptor = entityDescriptor.getAffiliationDescriptor();
        if (affiliationDescriptor != null) {
            if (RegexUtils.find(this.group, affiliationDescriptor.getID())
                || RegexUtils.find(this.group, affiliationDescriptor.getOwnerID())) {
                return authorizeReleaseOfAllowedAttributes(context, attributes);
            }
        }
        if (entityDescriptor.getParent() instanceof final EntitiesDescriptor ed
            && RegexUtils.find(this.group, ed.getName())) {
            return authorizeReleaseOfAllowedAttributes(context, attributes);
        }
        return new HashMap<>();
    }
}
