package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.util.RegexUtils;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Matches RFC 8707 resource indicators against the regular expressions a service allows, the same way for the
 * service's {@code allowedResources} and for those of its token exchange policy. Each requested resource must match
 * one of the expressions in full, case-sensitively, as URI paths are.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class RegisteredServiceOAuthResourceMatcher {

    /**
     * Whether the value is a well-formed resource indicator: an absolute URI without a fragment (RFC 8707, section 2).
     *
     * @param resource the requested resource
     * @return true if well-formed
     */
    public static boolean isValidResource(final String resource) {
        try {
            val uri = new URI(resource);
            return uri.isAbsolute() && uri.getFragment() == null;
        } catch (final URISyntaxException e) {
            return false;
        }
    }

    /**
     * Find the first allowed expression the resource fully matches.
     *
     * @param allowedResources the allowed resources, as regular expressions
     * @param resource         the requested resource
     * @return the matching expression, if any
     */
    public static Optional<String> findAllowedResource(final Collection<String> allowedResources, final String resource) {
        return allowedResources.stream()
            .filter(pattern -> RegexUtils.matches(RegexUtils.createPattern(pattern, RegexUtils.FLAG_CASE_SENSITIVE), resource))
            .findFirst();
    }

    /**
     * Whether the resource fully matches one of the allowed expressions.
     *
     * @param allowedResources the allowed resources, as regular expressions
     * @param resource         the requested resource
     * @return true if allowed
     */
    public static boolean isResourceAllowed(final Collection<String> allowedResources, final String resource) {
        return findAllowedResource(allowedResources, resource).isPresent();
    }

    /**
     * Whether every requested resource fully matches one of the allowed expressions.
     *
     * @param allowedResources the allowed resources, as regular expressions
     * @param resources        the requested resources
     * @return true if all are allowed
     */
    public static boolean areResourcesAllowed(final Collection<String> allowedResources, final Collection<String> resources) {
        return resources.stream().allMatch(resource -> isResourceAllowed(allowedResources, resource));
    }
}
