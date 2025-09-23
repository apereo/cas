package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.opensaml.saml.saml2.core.Attribute;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link BaseEntityCategoryAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public abstract class BaseEntityCategoryAttributeReleasePolicy extends MetadataEntityAttributesAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = 1532960981124784595L;

    private boolean useUniformResourceName;

    public BaseEntityCategoryAttributeReleasePolicy() {
        setAllowedAttributes(new ArrayList<>(getEntityCategoryAttributes().keySet()));
    }

    @JsonIgnore
    @Override
    public String getEntityAttribute() {
        return "http://macedir.org/entity-category";
    }

    @JsonIgnore
    @Override
    public String getEntityAttributeFormat() {
        return Attribute.URI_REFERENCE;
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }

    @Override
    protected Map<String, List<Object>> authorizeReleaseOfAllowedAttributes(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> attrs) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = new HashMap<String, List<Object>>();
        getEntityCategoryAttributes().forEach((key, value) -> {
            if (resolvedAttributes.containsKey(key)) {
                val attributeName = this.useUniformResourceName ? value : key;
                attributesToRelease.put(attributeName, resolvedAttributes.get(key));
            }
        });
        return attributesToRelease;
    }

    @Override
    protected List<String> determineRequestedAttributeDefinitions(
        final RegisteredServiceAttributeReleasePolicyContext context) {
        return this.useUniformResourceName
            ? new ArrayList<>(getEntityCategoryAttributes().values())
            : new ArrayList<>(getEntityCategoryAttributes().keySet());
    }

    @JsonIgnore
    protected abstract Map<String, String> getEntityCategoryAttributes();
}
