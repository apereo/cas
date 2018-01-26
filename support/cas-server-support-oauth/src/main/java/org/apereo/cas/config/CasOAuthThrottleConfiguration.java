package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

import static org.apereo.cas.support.oauth.OAuth20Constants.BASE_OAUTH20_URL;

/**
 * This is {@link CasOAuthThrottleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Configuration("oauthThrottleConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthThrottleConfiguration extends WebMvcConfigurerAdapter {

    @Autowired(required = false)
    @Qualifier("authenticationThrottle")
    private ThrottledSubmissionHandlerInterceptor authenticationThrottle;

    @Autowired
    @Qualifier("oauthHandlerInterceptorAdapter")
    private HandlerInterceptorAdapter oauthHandlerInterceptorAdapter;

    @ConditionalOnMissingBean(name = "oauthInterceptor")
    @Bean
    public HandlerInterceptorAdapter oauthInterceptor() {
        if (authenticationThrottle == null) {
            return oauthHandlerInterceptorAdapter;
        }
        return new OAuth20ThrottledHandlerInterceptorAdapter();
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(oauthInterceptor()).addPathPatterns(BASE_OAUTH20_URL.concat("/").concat("*"));
    }

    /**
     * This is {@link OAuth20ThrottledHandlerInterceptorAdapter} that acts as a wrapper around existing interceptors.
     *
     * @author Misagh Moayyed
     * @since 5.3.0
     */
    public class OAuth20ThrottledHandlerInterceptorAdapter extends HandlerInterceptorAdapter {
        private final Pattern pattern;

        public OAuth20ThrottledHandlerInterceptorAdapter() {
            final String throttledUrl = OAuth20Constants.BASE_OAUTH20_URL.concat("/")
                .concat(OAuth20Constants.ACCESS_TOKEN_URL + "|" + OAuth20Constants.TOKEN_URL);
            this.pattern = RegexUtils.createPattern(throttledUrl);
            LOGGER.debug("Authentication throttler instance for OAuth shall intercept the URL pattern [{}]", pattern.pattern());
        }

        @Override
        public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
            if (RegexUtils.matches(pattern, request.getServletPath()) && !authenticationThrottle.preHandle(request, response, handler)) {
                LOGGER.trace("OAuth authentication throttler prevented the request at [{}]", request.getServletPath());
                return false;
            }
            return oauthHandlerInterceptorAdapter.preHandle(request, response, handler);
        }

        @Override
        public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
                               final ModelAndView modelAndView) {
            if (RegexUtils.matches(this.pattern, request.getServletPath())) {
                LOGGER.trace("OAuth authentication throttler post-processing the request at [{}]", request.getServletPath());
                authenticationThrottle.postHandle(request, response, handler, modelAndView);
            }
        }
    }
}
