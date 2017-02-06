package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link InCommonRSRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InCommonRSRegisteredServiceAttributeReleasePolicy extends ReturnAllowedAttributeReleasePolicy {
    private static final long serialVersionUID = 1532960981124784595L;

    private List<String> allowedAttributes = Arrays.asList("eduPersonPrincipalName",
            "eduPersonTargetedID", "email", "displayName", "givenName", "surname",
            "eduPersonScopedAffiliation");

    public InCommonRSRegisteredServiceAttributeReleasePolicy() {
        setAllowedAttributes(allowedAttributes);
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Map<String, Object> attributes) {
        final EntityAttributesPredicate.Candidate attr =
                new EntityAttributesPredicate.Candidate("http://macedir.org/entity-category");
        attr.setValues(Collections.singletonList("http://refeds.org/category/research-and-scholarship"));

        final EntityAttributesPredicate predicate = new EntityAttributesPredicate(
                Collections.singletonList(attr), true);

        final EntityDescriptor input = ...
        if (predicate.apply(input)) {
            return super.getAttributesInternal(attributes);
        }
        return new HashMap();
    }
}
