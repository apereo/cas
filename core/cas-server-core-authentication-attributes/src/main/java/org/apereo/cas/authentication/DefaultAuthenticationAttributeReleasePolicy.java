package org.apereo.cas.authentication;

import lombok.Setter;
import lombok.val;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default AuthenticationAttributeReleasePolicy implementation.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
@Setter
public class DefaultAuthenticationAttributeReleasePolicy implements AuthenticationAttributeReleasePolicy {

    private Collection<String> attributesToRelease;

    private Set<String> attributesToNeverRelease = new HashSet<>();

    /**
     * Add additional attributes that should never be released in a validation response.
     *
     * @param attrs Additional attributes to never release
     */
    public void addAttributesToNeverRelease(final Collection<String> attrs) {
        if (attrs != null) {
            attributesToNeverRelease.addAll(attrs);
        }
    }

    /**
     * Return authentications attributes that we are allowed to release to client systems.
     *
     * @param authentication The authentication object we are processing.
     * @return The attributes to be released
     */
    @Override
    public Map<String, Object> getAuthenticationAttributesForRelease(final Authentication authentication) {
        val attrs = new HashMap<String, Object>(authentication.getAttributes());
        // remove any attributes explicitly prohibited
        attrs.keySet().removeAll(attributesToNeverRelease);
        // only apply whitelist if it contains attributes
        if (attributesToRelease != null && !attributesToRelease.isEmpty()) {
            attrs.keySet().retainAll(attributesToRelease);
        }
        return attrs;
    }
}
