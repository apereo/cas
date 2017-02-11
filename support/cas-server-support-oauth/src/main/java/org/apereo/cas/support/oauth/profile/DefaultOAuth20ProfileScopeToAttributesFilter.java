package org.apereo.cas.support.oauth.profile;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.pac4j.core.context.J2EContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultOAuth20ProfileScopeToAttributesFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultOAuth20ProfileScopeToAttributesFilter implements OAuth20ProfileScopeToAttributesFilter {
    @Override
    public Principal filter(final Service service, final Principal profile,
                            final RegisteredService registeredService,
                            final J2EContext context) {
        return profile;
    }

    /**
     * Gets attributes.
     *
     * @param attributes the attributes
     * @param context    the context
     * @return the attributes
     */
    protected Map<String, Object> getRequestParameters(final Collection<String> attributes, final J2EContext context) {
        return attributes.stream()
                .filter(a -> StringUtils.isNotBlank(context.getRequestParameter(a)))
                .map(m -> Pair.of(m, Arrays.asList(context.getRequest().getParameterValues(m))))
                .collect(Collectors.toMap(Pair::getKey, p -> p));
    }

    /**
     * Gets requested scopes.
     *
     * @param context the context
     * @return the requested scopes
     */
    protected Collection<String> getRequestedScopes(final J2EContext context) {
        final Map<String, Object> map = getRequestParameters(Arrays.asList(OAuthConstants.SCOPE), context);
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        return (Collection<String>) map.get(OAuthConstants.SCOPE);
    }
}
