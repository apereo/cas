package org.apereo.cas.services.web.support;

import org.apereo.cas.authentication.Authentication;

import javax.annotation.Nonnull;
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
public class DefaultAuthenticationAttributeReleasePolicy implements AuthenticationAttributeReleasePolicy {
    private Collection<String> attributesToRelease;

    @Nonnull
    private Set<String> attributesToNeverRelease = new HashSet<>();

    public void setAttributesToRelease(final Collection<String> attrs) {
        attributesToRelease = attrs;
    }

    public void setAttributesToNeverRelease(final Collection<String> attrs) {
        attributesToNeverRelease = attrs != null ? new HashSet<>(attrs) : new HashSet<>();
    }

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
    public Map<String, Object> getAuthenticationAttributesForRelease(@Nonnull final Authentication authentication) {
        final HashMap<String, Object> attrs = new HashMap<>(authentication.getAttributes());

        // remove any attributes explicitly prohibited
        attrs.keySet().removeAll(attributesToNeverRelease);

        // only apply whitelist if it contains attributes
        if (attributesToRelease != null && !attributesToRelease.isEmpty()) {
            attrs.keySet().retainAll(attributesToRelease);
        }

        return attrs;
    }
}
