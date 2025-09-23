package org.apereo.cas.throttle;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
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
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
            LOGGER.warn("Throttling submission from [{}]. Authentication attempt exceeds the failure threshold [{}]",
                request.getRemoteAddr(), this.thresholdRate);
            recordThrottle(request);
            updateThrottledSubmission(request);
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
            LOGGER.debug("Recording submission failure for request URI [{}]", request.getRequestURI());
            recordSubmissionFailure(request);
        } else {
            LOGGER.trace("Skipping to record submission failure for [{}] with response status [{}]",
                request.getRequestURI(), response.getStatus());
        }
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception e) {
        if (!isRequestIgnoredForThrottling(request, response) && shouldResponseBeRecordedAsFailure(response)) {
            recordSubmissionFailure(request);
        }
    }

    protected boolean throttleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val executor = configurationContext.getThrottledRequestExecutor();
        return executor != null && executor.throttle(request, response);
    }

    protected boolean shouldResponseBeRecordedAsFailure(final HttpServletResponse response) {
        val status = response.getStatus();
        return status != HttpStatus.CREATED.value()
            && status != HttpStatus.OK.value() && status != HttpStatus.FOUND.value();
    }

    protected void recordThrottle(final HttpServletRequest request) {
    }

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

    protected String getUsernameParameterFromRequest(final HttpServletRequest request) {
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getCore();
        return request.getParameter(StringUtils.defaultIfBlank(throttle.getUsernameParameter(), "username"));
    }

    protected LocalDateTime getFailureInRangeCutOffDate() {
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getFailure();
        return LocalDateTime.now(ZoneOffset.UTC).minusSeconds(throttle.getRangeSeconds());
    }

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

    protected ThrottledSubmission toThrottledSubmission(
        final AuditActionContext context) {
        return ThrottledSubmission.builder()
            .key(UUID.randomUUID().toString())
            .value(context.getWhenActionWasPerformed().atZone(ZoneOffset.UTC))
            .build();
    }

    protected void updateThrottledSubmission(final HttpServletRequest request) {
        val duration = Beans.newDuration(getConfigurationContext().getCasProperties()
            .getAuthn().getThrottle().getFailure().getThrottleWindowSeconds());
        val expiration = ZonedDateTime.now(Clock.systemUTC()).plusSeconds(duration.toSeconds());
        val submission = (ThrottledSubmission) request.getAttribute(ThrottledSubmission.class.getSimpleName());
        if (submission != null && !submission.isStillInExpirationWindow()) {
            submission.setExpiration(expiration);
            LOGGER.info("Updated throttled submission's expiration date: [{}]", submission);
            getConfigurationContext().getThrottledSubmissionStore().put(submission);
        }
    }
}
