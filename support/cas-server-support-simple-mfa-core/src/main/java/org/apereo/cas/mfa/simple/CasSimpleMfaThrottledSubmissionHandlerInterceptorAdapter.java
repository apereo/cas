package org.apereo.cas.mfa.simple;

import org.apereo.cas.configuration.model.support.mfa.CasSimpleMultifactorProperties;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Abstract implementation of the handler that has all of the logic.  Encapsulates the logic in case we get it wrong!
 *
 * @author Fotis Memis
 * @since 6.3.0
 */

@Slf4j
@ToString
@Getter
@RequiredArgsConstructor
public class CasSimpleMfaThrottledSubmissionHandlerInterceptorAdapter extends HandlerInterceptorAdapter implements ThrottledSubmissionHandlerInterceptor {
    private final ConcurrentMap<String, ZonedDateTime> userMap;
    private final CasSimpleMultifactorProperties properties;
    private final ThrottledRequestResponseHandler throttledRequestResponseHandler;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {

        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            LOGGER.trace("Letting the request through given http method is [{}]", request.getMethod());
            return true;
        }
        if (shouldSimpleMfaThrottleCheckBePerformed(request)) {
            val sessionId = request.getSession().getId();
            if (sessionId != null && throttleSmsOrEmail(sessionId, userMap, properties)) {
                return this.throttledRequestResponseHandler.handle(request, response);
            }
        }
        return true;
    }

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final ModelAndView modelAndView) {

        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            LOGGER.trace("Letting the request through given http method is [{}]", request.getMethod());
            return;
        }
        if (shouldSimpleMfaThrottleCheckBePerformed(request)) {
            val sessionId = request.getSession().getId();
            if (sessionId != null) {
                userMap.put(sessionId, ZonedDateTime.now(ZoneOffset.UTC));
            }
        }
        return;
    }

    public boolean throttleSmsOrEmail(final String sessionId, final ConcurrentMap<String, ZonedDateTime> userMap, final CasSimpleMultifactorProperties properties) {
        return userMap.containsKey(sessionId) && !resendAllowed(userMap.get(sessionId), properties);
    }

    public boolean resendAllowed(final ZonedDateTime time, final CasSimpleMultifactorProperties properties) {
        return ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli() > time.toInstant().toEpochMilli()
                + TimeUnit.SECONDS.toMillis(properties.getResendTimeInSeconds());

    }

    public boolean shouldSimpleMfaThrottleCheckBePerformed(final HttpServletRequest request) {
        return ((request.getParameter("username") != null) && request.getParameter("password") != null)
            || (request.getParameter("_eventId") != null && request.getParameter("_eventId").equals("resend"));
    }

    public void decrement() {
        LOGGER.debug("Beginning simple mfa user map cleanup...");
        this.userMap.entrySet().removeIf(entry -> ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli() > entry.getValue().toInstant().toEpochMilli()
            + TimeUnit.SECONDS.toMillis(properties.getResendTimeInSeconds()));
        LOGGER.debug("Done decrementing count for simple-mfa throttler.");
    }



}



