package org.apereo.cas.web;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.Config;
import org.pac4j.core.matching.matcher.DefaultMatchers;
import org.pac4j.jee.context.JEEFrameworkParameters;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link SecurityLogicInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class SecurityLogicInterceptor implements HandlerInterceptor {
    private final Config config;
    private final String clientNames;

    @Override
    public boolean preHandle(final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response, final @NonNull Object handler) {
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(this.config);
        val result = config.getSecurityLogic().perform(this.config, (ctx, session, profiles) -> true,
            this.clientNames, DefaultAuthorizers.IS_FULLY_AUTHENTICATED, DefaultMatchers.SECURITYHEADERS, new JEEFrameworkParameters(request, response));
        return result != null && Boolean.parseBoolean(result.toString());
    }

}
