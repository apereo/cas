package org.apereo.cas.adaptors.trusted.authentication.principal;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This is {@link DefaultRemoteRequestPrincipalAttributesExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class DefaultRemoteRequestPrincipalAttributesExtractor implements RemoteRequestPrincipalAttributesExtractor {
    private final Map<String, String> headerPatterns;

    @Override
    public Map<String, List<Object>> getAttributes(final HttpServletRequest request) {
        val attributes = new HashMap<String, List<Object>>();
        val headers = Collections.list(request.getHeaderNames());
        headers.forEach(headerName -> headerPatterns.entrySet()
            .stream()
            .filter(entry -> RegexUtils.find(entry.getKey(), headerName))
            .filter(entry -> StringUtils.isNotBlank(request.getHeader(headerName)))
            .filter(entry -> RegexUtils.find(entry.getValue(), request.getHeader(headerName)))
            .forEach(entry -> {
                val headerNamePattern = RegexUtils.createPattern(entry.getKey(), Pattern.CASE_INSENSITIVE);
                val headerNameMatcher = headerNamePattern.matcher(headerName);
                val headerNameToUse = headerNameMatcher.find() && headerNameMatcher.groupCount() > 0
                    ? headerNameMatcher.group(1) : headerName;
                val headerValue = request.getHeader(headerName);
                val headerValuePattern = RegexUtils.createPattern(entry.getValue(), Pattern.CASE_INSENSITIVE);
                val headerValueMatcher = headerValuePattern.matcher(headerValue);
                val headerValueToUse = headerValueMatcher.find() && headerValueMatcher.groupCount() > 0
                    ? headerValueMatcher.group(1) : headerName;
                attributes.put(headerNameToUse, CollectionUtils.wrapList(headerValueToUse));
            }));
        return attributes;
    }
}
