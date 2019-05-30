package org.apereo.cas.web.support;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * Abstract implementation of the handler that has all of the logic.  Encapsulates the logic in case we get it wrong!
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Slf4j
@ToString
@Getter
@RequiredArgsConstructor
public abstract class AbstractThrottledSubmissionHandlerInterceptorAdapter extends HandlerInterceptorAdapter
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
        this.thresholdRate = (double) configurationContext.getFailureThreshold() / configurationContext.getFailureRangeInSeconds();
        LOGGER.trace("Calculated threshold rate as [{}]", this.thresholdRate);
    }

    @Override
    public final boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                                   final Object o) throws Exception {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            LOGGER.trace("Letting the request through given http method is [{}]", request.getMethod());
            return true;
        }

        val throttled = throttleRequest(request, response) || exceedsThreshold(request);
        if (throttled) {
            LOGGER.warn("Throttling submission from [{}]. More than [{}] failed login attempts within [{}] seconds. "
                    + "Authentication attempt exceeds the failure threshold [{}]", request.getRemoteAddr(),
                this.thresholdRate, configurationContext.getFailureRangeInSeconds(), configurationContext.getFailureThreshold());

            recordThrottle(request);
            return configurationContext.getThrottledRequestResponseHandler().handle(request, response);
        }
        return true;
    }

    /**
     * Is request throttled.
     *
     * @param request  the request
     * @param response the response
     * @return true if the request is throttled. False otherwise, letting it proceed.
     */
    protected boolean throttleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        return configurationContext.getThrottledRequestExecutor() != null
            && configurationContext.getThrottledRequestExecutor().throttle(request, response);
    }

    @Override
    public final void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                                 final Object o, final ModelAndView modelAndView) {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            LOGGER.trace("Skipping authentication throttling for requests other than POST");
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

    /**
     * Should response be recorded as failure boolean.
     *
     * @param response the response
     * @return the boolean
     */
    protected boolean shouldResponseBeRecordedAsFailure(final HttpServletResponse response) {
        val status = response.getStatus();
        return status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK && status != HttpStatus.SC_MOVED_TEMPORARILY;
    }

    /**
     * Record throttling event.
     *
     * @param request the request
     */
    protected void recordThrottle(final HttpServletRequest request) {
    }

    @Override
    public void decrement() {
        LOGGER.debug("Throttling is not activated for this interceptor adapter");
    }

    /**
     * Calculate threshold rate and compare boolean.
     * Compute rate in submissions/sec between last two authn failures and compare with threshold.
     *
     * @param failures the failures
     * @return the boolean
     */
    protected boolean calculateFailureThresholdRateAndCompare(final List<Date> failures) {
        if (failures.size() < 2) {
            return false;
        }
        val lastTime = failures.get(0).getTime();
        val secondToLastTime = failures.get(1).getTime();
        val difference = lastTime - secondToLastTime;
        val rate = NUMBER_OF_MILLISECONDS_IN_SECOND / difference;
        LOGGER.debug("Last attempt was at [{}] and the one before that was at [{}]. Difference is [{}] calculated as rate of [{}]",
            lastTime, secondToLastTime, difference, rate);
        if (rate > getThresholdRate()) {
            LOGGER.warn("Authentication throttling rate [{}] exceeds the defined threshold [{}]", rate, getThresholdRate());
            return true;
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
        return request.getParameter(StringUtils.defaultString(configurationContext.getUsernameParameter(), "username"));
    }

    /**
     * Gets failure in range cut off date.
     *
     * @return the failure in range cut off date
     */
    protected Date getFailureInRangeCutOffDate() {
        val cutoff = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(configurationContext.getFailureRangeInSeconds());
        return DateTimeUtils.timestampOf(cutoff);
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
        val resource = StringUtils.defaultString(request.getParameter(CasProtocolConstants.PARAMETER_SERVICE), "N/A");
        val context = new AuditActionContext(
            userToUse,
            resource,
            actionName,
            configurationContext.getApplicationCode(),
            DateTimeUtils.dateOf(ZonedDateTime.now(ZoneOffset.UTC)),
            clientInfo.getClientIpAddress(),
            clientInfo.getServerIpAddress());
        LOGGER.debug("Recording throttled audit action [{}]", context);
        configurationContext.getAuditTrailExecutionPlan().record(context);
    }
}
