package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ThrottledSubmissionFailureRecordingTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("AuthenticationThrottling")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.throttle.failure.range-seconds=3",
        "cas.authn.throttle.failure.threshold=3"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ThrottledSubmissionFailureRecordingTests {
    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;

    @Autowired
    @Qualifier(ThrottledSubmissionsStore.BEAN_NAME)
    private ThrottledSubmissionsStore<ThrottledSubmission> throttleSubmissionStore;

    private static MockHttpServletRequest newRequest(final String address) {
        val request = new MockHttpServletRequest(HttpMethod.POST.name(), "/cas/v1/tickets");
        request.setRemoteAddr(address);
        request.setLocalAddr(address);
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        return request;
    }

    private static MockHttpServletResponse newResponse(final int status) {
        val response = new MockHttpServletResponse();
        response.setStatus(status);
        return response;
    }

    @Test
    void verifyUnauthorizedIsRecordedOnCompletionOnly() {
        val address = "10.10.0.1";
        val request = newRequest(address);
        val response = newResponse(401);
        assertTrue(throttle.preHandle(request, response, throttle));
        throttle.postHandle(request, response, throttle, null);
        assertFalse(throttleSubmissionStore.contains(address));
        throttle.afterCompletion(request, response, throttle, null);
        assertTrue(throttleSubmissionStore.contains(address));
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 204, 302, 303, 400, 403, 404, 405, 500, 503})
    void verifyOtherStatusesAreNotRecorded(final int status) {
        val address = "10.20.%s.%s".formatted(status / 100, status % 100);
        val request = newRequest(address);
        val response = newResponse(status);
        throttle.postHandle(request, response, throttle, null);
        throttle.afterCompletion(request, response, throttle, null);
        assertFalse(throttleSubmissionStore.contains(address));
    }

    @Test
    void verifyMarkedFailureIsRecorded() {
        val address = "10.30.0.1";
        val request = newRequest(address);
        ThrottledSubmissionHandlerInterceptor.markAuthenticationFailure(request);
        assertTrue(ThrottledSubmissionHandlerInterceptor.isAuthenticationFailure(request));
        throttle.afterCompletion(request, newResponse(500), throttle, null);
        assertTrue(throttleSubmissionStore.contains(address));
    }
}
