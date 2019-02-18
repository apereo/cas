package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

/**
 * This is {@link RefedsRSAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class RefedsRSAttributeReleasePolicy extends InCommonRSAttributeReleasePolicy {
    private static final long serialVersionUID = 2532960981124784595L;

    @JsonIgnore
    @Override
    public Set<String> getEntityAttributeValues() {
        return CollectionUtils.wrapSet("http://refeds.org/category/research-and-scholarship");
    }
}
