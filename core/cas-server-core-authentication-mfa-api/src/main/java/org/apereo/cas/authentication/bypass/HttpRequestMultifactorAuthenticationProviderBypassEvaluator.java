package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RegexUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Multifactor Bypass provider based on HttpRequest.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
public class HttpRequestMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    private static final long serialVersionUID = -7553981418344342672L;

    private final MultifactorAuthenticationProviderBypassProperties bypassProperties;
    private final Pattern httpRequestRemoteAddressPattern;
    private final Set<Pattern> httpRequestHeaderPatterns;

    public HttpRequestMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassProperties bypassProperties,
                                                                       final String providerId) {
        super(providerId);
        this.bypassProperties = bypassProperties;
        if (StringUtils.isNotBlank(bypassProperties.getHttpRequestRemoteAddress())) {
            this.httpRequestRemoteAddressPattern = RegexUtils.createPattern(bypassProperties.getHttpRequestRemoteAddress());
        } else {
            this.httpRequestRemoteAddressPattern = RegexUtils.MATCH_NOTHING_PATTERN;
        }

        val values = org.springframework.util.StringUtils.commaDelimitedListToSet(bypassProperties.getHttpRequestHeaders());
        this.httpRequestHeaderPatterns = values.stream().map(RegexUtils::createPattern).collect(Collectors.toSet());
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                  final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  final HttpServletRequest request) {
        val principal = authentication.getPrincipal();
        val bypassByHttpRequest = locateMatchingHttpRequest(authentication, request);
        if (bypassByHttpRequest) {
            LOGGER.debug("Bypass rules for http request indicate the request may be ignored for [{}]", principal.getId());
            return false;
        }

        return true;
    }

    /**
     * Locate matching http request and determine if bypass should be enabled.
     *
     * @param authentication the authentication
     * @param request        the request
     * @return true /false
     */
    protected boolean locateMatchingHttpRequest(final Authentication authentication, final HttpServletRequest request) {
        if (StringUtils.isNotBlank(bypassProperties.getHttpRequestRemoteAddress())) {
            if (httpRequestRemoteAddressPattern.matcher(request.getRemoteAddr()).find()) {
                LOGGER.debug("Http request remote address [{}] matches [{}]", bypassProperties.getHttpRequestRemoteAddress(), request.getRemoteAddr());
                return true;
            }
            if (httpRequestRemoteAddressPattern.matcher(request.getRemoteHost()).find()) {
                LOGGER.debug("Http request remote host [{}] matches [{}]", bypassProperties.getHttpRequestRemoteAddress(), request.getRemoteHost());
                return true;
            }
        }

        if (StringUtils.isNotBlank(bypassProperties.getHttpRequestHeaders())) {
            val headerNames = Collections.list(request.getHeaderNames());
            val matched = this.httpRequestHeaderPatterns.stream()
                .anyMatch(pattern -> headerNames.stream().anyMatch(pattern.asMatchPredicate()));
            if (matched) {
                LOGGER.debug("Http request remote headers [{}] match [{}]", headerNames, bypassProperties.getHttpRequestHeaders());
                return true;
            }
        }

        return false;
    }
}
