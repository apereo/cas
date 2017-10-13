package org.apereo.cas.adaptors.trusted.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is {@link ShibbolethServiceProviderRequestPrincipalAttributesExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ShibbolethServiceProviderRequestPrincipalAttributesExtractor implements RemoteRequestPrincipalAttributesExtractor {
    private static final String PREFIX = "AJP_";

    @Override
    public Map<String, Object> getAttributes(final HttpServletRequest request) {
        return Collections.list(request
                .getHeaderNames())
                .stream()
                .filter(t -> t.toUpperCase().startsWith(PREFIX))
                .filter(t -> StringUtils.isNotBlank(request.getHeader(t)))
                .map(t -> StringUtils.removeAll(t, PREFIX))
                .collect(Collectors.toMap(Function.identity(),
                    t -> CollectionUtils.wrap(request.getHeader(PREFIX + t).split("(?<!\\\\);"))));
    }
}
