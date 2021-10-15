package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.opensaml.saml.saml2.core.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This is {@link InCommonRSAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InCommonRSAttributeReleasePolicy extends MetadataEntityAttributesAttributeReleasePolicy {
    /**
     * Map of allowed attributes by this policy in the form of attribute name linked
     * to its equivalent urn value.
     */
    public static final Map<String, String> ALLOWED_ATTRIBUTES = CollectionUtils.wrap(
        "eduPersonPrincipalName", "urn:oid:1.3.6.1.4.1.5923.1.1.1.6",
        "eduPersonTargetedID", "urn:oid:1.3.6.1.4.1.5923.1.1.1.10",
        "email", "urn:oid:0.9.2342.19200300.100.1.3",
        "mail", "urn:oid:0.9.2342.19200300.100.1.3",
        "displayName", "urn:oid:2.16.840.1.113730.3.1.241",
        "givenName", "urn:oid:2.5.4.42",
        "surname", "urn:oid:2.5.4.4",
        "sn", "urn:oid:2.5.4.4",
        "eduPersonScopedAffiliation", "urn:oid:1.3.6.1.4.1.5923.1.1.1.9");

    private static final long serialVersionUID = 1532960981124784595L;

    @Getter
    @Setter
    private boolean useUniformResourceName;

    public InCommonRSAttributeReleasePolicy() {
        setAllowedAttributes(new ArrayList<>(ALLOWED_ATTRIBUTES.keySet()));
    }

    @JsonIgnore
    @Override
    public String getEntityAttribute() {
        return "http://macedir.org/entity-category";
    }

    @JsonIgnore
    @Override
    public Set<String> getEntityAttributeValues() {
        return CollectionUtils.wrapSet("http://id.incommon.org/category/research-and-scholarship");
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
        final Principal principal,
        final Map<String, List<Object>> attrs,
        final RegisteredService registeredService,
        final Service selectedService) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = new HashMap<String, List<Object>>();
        ALLOWED_ATTRIBUTES.forEach((key, value) -> {
            if (resolvedAttributes.containsKey(key)) {
                val attributeName = this.useUniformResourceName ? value : key;
                attributesToRelease.put(attributeName, resolvedAttributes.get(key));
            }
        });
        return attributesToRelease;
    }

    @Override
    protected List<String> determineRequestedAttributeDefinitions(
        final Principal principal,
        final RegisteredService registeredService,
        final Service selectedService) {
        return this.useUniformResourceName
            ? new ArrayList<>(ALLOWED_ATTRIBUTES.values())
            : new ArrayList<>(ALLOWED_ATTRIBUTES.keySet());
    }
}
