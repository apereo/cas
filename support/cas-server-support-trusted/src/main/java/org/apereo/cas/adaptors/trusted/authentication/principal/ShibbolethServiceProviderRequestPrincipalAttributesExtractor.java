package org.apereo.cas.adaptors.trusted.authentication.principal;

import org.apereo.cas.util.CollectionUtils;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link ShibbolethServiceProviderRequestPrincipalAttributesExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ShibbolethServiceProviderRequestPrincipalAttributesExtractor implements RemoteRequestPrincipalAttributesExtractor {
    private static final String PREFIX = "AJP_";
    private static final Pattern PATTERN_SHIBBOLETH_HEADER = Pattern.compile("(?<!\\\\);");

    @Override
    public Map<String, List<Object>> getAttributes(final HttpServletRequest request) {
        return Collections.list(request
            .getHeaderNames())
            .stream()
            .filter(t -> t.toUpperCase().startsWith(PREFIX))
            .filter(t -> StringUtils.isNotBlank(request.getHeader(t)))
            .map(t -> RegExUtils.removeAll(t, PREFIX))
            .collect(Collectors.toMap(Function.identity(),
                t -> CollectionUtils.wrap(PATTERN_SHIBBOLETH_HEADER.split(request.getHeader(PREFIX + t)))));
    }
}
