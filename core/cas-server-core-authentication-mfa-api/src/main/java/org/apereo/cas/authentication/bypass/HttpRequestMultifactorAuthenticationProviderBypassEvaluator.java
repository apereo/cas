package org.apereo.cas.authentication.bypass;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Multifactor Bypass provider based on HttpRequest.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
public class HttpRequestMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = -7553981418344342672L;

    private final MultifactorAuthenticationProviderBypassProperties bypassProperties;

    private final Pattern httpRequestRemoteAddressPattern;

    private final Set<Pattern> httpRequestHeaderPatterns;

    public HttpRequestMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassProperties bypassProperties,
                                                                       final String providerId, final ApplicationContext applicationContext) {
        super(providerId, applicationContext);
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
                                                                          @Nullable final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          @Nullable final HttpServletRequest request) {
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
    protected boolean locateMatchingHttpRequest(final Authentication authentication, @Nullable final HttpServletRequest request) {
        if (StringUtils.isNotBlank(bypassProperties.getHttpRequestRemoteAddress()) && request != null) {
            if (httpRequestRemoteAddressPattern.matcher(request.getRemoteAddr()).find()) {
                LOGGER.debug("Http request remote address [{}] matches [{}]", bypassProperties.getHttpRequestRemoteAddress(), request.getRemoteAddr());
                return true;
            }
            if (httpRequestRemoteAddressPattern.matcher(request.getRemoteHost()).find()) {
                LOGGER.debug("Http request remote host [{}] matches [{}]", bypassProperties.getHttpRequestRemoteAddress(), request.getRemoteHost());
                return true;
            }
        }

        if (StringUtils.isNotBlank(bypassProperties.getHttpRequestHeaders()) && request != null) {
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
