package org.apereo.cas.support.saml.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link MetadataEntityAttributesAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MetadataEntityAttributesAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(InCommonRSAttributeReleasePolicy.class);
    private static final long serialVersionUID = -3483733307124962357L;

    private String entityAttribute;
    private String entityAttributeFormat;
    private Set<String> entityAttributeValues = new LinkedHashSet<>();
    
    @Override
    protected Map<String, Object> getAttributesForSamlRegisteredService(final Map<String, Object> attributes, final SamlRegisteredService service, 
                                                                        final ApplicationContext applicationContext, 
                                                                        final SamlRegisteredServiceCachingMetadataResolver resolver, 
                                                                        final SamlRegisteredServiceServiceProviderMetadataFacade facade, 
                                                                        final EntityDescriptor entityDescriptor) {

        final EntityAttributesPredicate.Candidate attr = new EntityAttributesPredicate.Candidate(this.entityAttribute, this.entityAttributeFormat);
        attr.setValues(this.entityAttributeValues);

        LOGGER.debug("Loading entity attribute predicate filter for candidate [{}] with values [{}]", attr.getName(), attr.getValues());
        final EntityAttributesPredicate predicate = new EntityAttributesPredicate(CollectionUtils.wrap(attr), true);
        
        if (predicate.apply(entityDescriptor)) {
            return authorizeReleaseOfAllowedAttributes(attributes);
        }
        return new HashMap<>(0);
    }

    public String getEntityAttribute() {
        return entityAttribute;
    }

    public void setEntityAttribute(final String entityAttribute) {
        this.entityAttribute = entityAttribute;
    }

    public String getEntityAttributeFormat() {
        return entityAttributeFormat;
    }

    public void setEntityAttributeFormat(final String entityAttributeFormat) {
        this.entityAttributeFormat = entityAttributeFormat;
    }

    public Set<String> getEntityAttributeValues() {
        return entityAttributeValues;
    }

    public void setEntityAttributeValues(final Set<String> entityAttributeValues) {
        this.entityAttributeValues = entityAttributeValues;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("entityAttribute", entityAttribute)
                .append("entityAttributeFormat", entityAttributeFormat)
                .append("entityAttributeValues", entityAttributeValues)
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
        final MetadataEntityAttributesAttributeReleasePolicy rhs = (MetadataEntityAttributesAttributeReleasePolicy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.entityAttribute, rhs.entityAttribute)
                .append(this.entityAttributeFormat, rhs.entityAttributeFormat)
                .append(this.entityAttributeValues, rhs.entityAttributeValues)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(entityAttribute)
                .append(entityAttributeFormat)
                .append(entityAttributeValues)
                .toHashCode();
    }
}
