package org.apereo.cas.web.support;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.util.DateTimeUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Abstract implementation of the handler that has all of the logic.  Encapsulates the logic in case we get it wrong!
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Slf4j
@ToString
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractThrottledSubmissionHandlerInterceptorAdapter
    implements ThrottledSubmissionHandlerInterceptor, InitializingBean {
    /**
     * Throttled login attempt action code used to tag the attempt in audit records.
     */
    public static final String ACTION_THROTTLED_LOGIN_ATTEMPT = "THROTTLED_LOGIN_ATTEMPT";

    /**
     * Number of milli-seconds in a second.
     */
    private static final double NUMBER_OF_MILLISECONDS_IN_SECOND = 1000.0;

    private final ThrottledSubmissionHandlerConfigurationContext configurationContext;

    private double thresholdRate = -1;

    @Override
    public void afterPropertiesSet() {
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getFailure();
        this.thresholdRate = (double) throttle.getThreshold() / throttle.getRangeSeconds();
        LOGGER.trace("Calculated threshold rate as [{}]", this.thresholdRate);
    }

    @Override
    public final boolean preHandle(final HttpServletRequest request,
                                   final HttpServletResponse response,
                                   final Object handler) {
        if (isRequestIgnoredForThrottling(request, response)) {
            LOGGER.trace("Letting the request through without throttling; No request filters support it");
            return true;
        }

        val throttled = throttleRequest(request, response) || exceedsThreshold(request);
        if (throttled) {
            val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getFailure();
            LOGGER.warn("Throttling submission from [{}]. More than [{}] failed login attempts within [{}] seconds. "
                        + "Authentication attempt exceeds the failure threshold [{}]", request.getRemoteAddr(),
                this.thresholdRate, throttle.getRangeSeconds(), throttle.getThreshold());

            recordThrottle(request);
            return configurationContext.getThrottledRequestResponseHandler().handle(request, response);
        }
        return true;
    }

    @Override
    public final void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                                 final Object handler, final ModelAndView modelAndView) {
        if (isRequestIgnoredForThrottling(request, response)) {
            LOGGER.trace("Skipping authentication throttling for requests; no filters support it.");
            return;
        }

        val recordEvent = shouldResponseBeRecordedAsFailure(response);
        if (recordEvent) {
            LOGGER.debug("Recording submission failure for [{}]", request.getRequestURI());
            recordSubmissionFailure(request);
        } else {
            LOGGER.trace("Skipping to record submission failure for [{}] with response status [{}]",
                request.getRequestURI(), response.getStatus());
        }
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception e) throws Exception {
        if (!isRequestIgnoredForThrottling(request, response) && shouldResponseBeRecordedAsFailure(response)) {
            recordSubmissionFailure(request);
        }
    }

    /**
     * Is request throttled.
     *
     * @param request  the request
     * @param response the response
     * @return true if the request is throttled. False otherwise, letting it proceed.
     */
    protected boolean throttleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val executor = configurationContext.getThrottledRequestExecutor();
        return executor != null && executor.throttle(request, response);
    }

    /**
     * Should response be recorded as failure boolean.
     *
     * @param response the response
     * @return true/false
     */
    protected boolean shouldResponseBeRecordedAsFailure(final HttpServletResponse response) {
        val status = response.getStatus();
        return status != HttpStatus.CREATED.value()
               && status != HttpStatus.OK.value() && status != HttpStatus.FOUND.value();
    }

    /**
     * Record throttling event.
     *
     * @param request the request
     */
    protected void recordThrottle(final HttpServletRequest request) {
    }

    /**
     * Calculate threshold rate and compare boolean.
     * Compute rate in submissions/sec between last two authn failures and compare with threshold.
     *
     * @param failures the failures
     * @return true/false
     */
    @SuppressWarnings("JavaUtilDate")
    protected boolean calculateFailureThresholdRateAndCompare(final List<? extends ThrottledSubmission> failures) {
        if (failures.size() >= 2) {
            val lastTime = DateTimeUtils.dateOf(failures.getFirst().getValue()).getTime();
            val secondToLastTime = DateTimeUtils.dateOf(failures.get(1).getValue()).getTime();
            val difference = lastTime - secondToLastTime;
            val rate = NUMBER_OF_MILLISECONDS_IN_SECOND / difference;
            LOGGER.debug("Last attempt was at [{}] and the one before that was at [{}]. Difference is [{}] calculated as rate of [{}]",
                lastTime, secondToLastTime, difference, rate);
            if (rate > getThresholdRate()) {
                LOGGER.warn("Authentication throttling rate [{}] exceeds the defined threshold [{}]", rate, getThresholdRate());
                return true;
            }
        }
        return false;
    }

    /**
     * Construct username from the request.
     *
     * @param request the request
     * @return the string
     */
    protected String getUsernameParameterFromRequest(final HttpServletRequest request) {
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getCore();
        return request.getParameter(StringUtils.defaultIfBlank(throttle.getUsernameParameter(), "username"));
    }

    /**
     * Gets failure in range cut off date.
     *
     * @return the failure in range cut off date
     */
    protected LocalDateTime getFailureInRangeCutOffDate() {
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getFailure();
        return LocalDateTime.now(ZoneOffset.UTC).minusSeconds(throttle.getRangeSeconds());
    }

    /**
     * Records an audit action.
     *
     * @param request    The current HTTP request.
     * @param actionName Name of the action to be recorded.
     */
    protected void recordAuditAction(final HttpServletRequest request, final String actionName) {
        val userToUse = getUsernameParameterFromRequest(request);
        val clientInfo = ClientInfoHolder.getClientInfo();
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getCore();
        val resource = StringUtils.defaultIfBlank(request.getParameter(CasProtocolConstants.PARAMETER_SERVICE), "N/A");
        val context = new AuditActionContext(
            userToUse,
            resource,
            actionName,
            throttle.getAppCode(),
            LocalDateTime.now(ZoneOffset.UTC),
            clientInfo);
        LOGGER.debug("Recording throttled audit action [{}]", context);
        configurationContext.getAuditTrailExecutionPlan().record(context);
    }

    private boolean isRequestIgnoredForThrottling(final HttpServletRequest request, final HttpServletResponse response) {
        val plan = configurationContext.getApplicationContext().getBean(AuthenticationThrottlingExecutionPlan.class);
        return !plan.getAuthenticationThrottleFilter().supports(request, response);
    }

    /**
     * To throttled submission.
     *
     * @param context the context
     * @return the throttled submission
     */
    protected ThrottledSubmission toThrottledSubmission(
        final AuditActionContext context) {
        return ThrottledSubmission.builder()
            .key(UUID.randomUUID().toString())
            .value(context.getWhenActionWasPerformed().atZone(ZoneOffset.UTC))
            .build();
    }
}
