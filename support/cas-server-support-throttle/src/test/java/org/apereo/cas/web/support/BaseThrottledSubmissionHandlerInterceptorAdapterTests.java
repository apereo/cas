package org.apereo.cas.web.support;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Base class for submission throttle tests.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuditConfiguration.class,
    AopAutoConfiguration.class,
    CasThrottlingConfiguration.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(properties = {"spring.aop.proxy-target-class=true", "cas.authn.throttle.failure.rangeSeconds=1", "cas.authn.throttle.failure.threshold=2"})
@EnableScheduling
@Slf4j
public abstract class BaseThrottledSubmissionHandlerInterceptorAdapterTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    protected static final String IP_ADDRESS = "1.2.3.4";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("casAuthenticationManager")
    protected AuthenticationManager authenticationManager;

    @BeforeEach
    public void initialize() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr(IP_ADDRESS);
        request.setLocalAddr(IP_ADDRESS);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }

    @AfterEach
    public void afterEachTest() {
        ClientInfoHolder.setClientInfo(null);
    }

    @Test
    public void verifyThrottle() throws Exception {
        // Ensure that repeated logins BELOW threshold rate are allowed
        failLoop(3, 1000, HttpStatus.SC_UNAUTHORIZED);

        // Ensure that repeated logins ABOVE threshold rate are throttled
        failLoop(3, 200, HttpStatus.SC_LOCKED);

        // Ensure that slowing down relieves throttle
        getThrottle().decrement();
        Thread.sleep(1000);
        failLoop(3, 1000, HttpStatus.SC_UNAUTHORIZED);
    }

    @SneakyThrows
    protected void failLoop(final int trials, final int period, final int expected) {
        // Seed with something to compare against
        loginUnsuccessfully("mog", "1.2.3.4");

        IntStream.range(0, trials).forEach(i -> {
            try {
                LOGGER.debug("Waiting for [{}] ms", period);
                Thread.sleep(period);
                val status = loginUnsuccessfully("mog", "1.2.3.4");
                if (i == trials) {
                    assertEquals(expected, status.getStatus());
                }
            } catch (final Exception e) {
                throw new AssertionError(e.getMessage(), e);
            }
        });
    }

    protected MockHttpServletResponse loginUnsuccessfully(final String username, final String fromAddress) throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setParameter("username", username);
        request.setRemoteAddr(fromAddress);
        request.setRequestURI("/cas/login");
        val context = new MockRequestContext();
        context.setCurrentEvent(new Event(StringUtils.EMPTY, "error"));
        request.setAttribute("flowRequestContext", context);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        getThrottle().preHandle(request, response, null);

        try {
            authenticationManager.authenticate(DefaultAuthenticationTransaction.of(CoreAuthenticationTestUtils.getService(), badCredentials(username)));
        } catch (final AuthenticationException e) {
            getThrottle().postHandle(request, response, null, null);
            return response;
        }
        throw new AssertionError("Expected AbstractAuthenticationException");
    }

    private static UsernamePasswordCredential badCredentials(final String username) {
        val credentials = new UsernamePasswordCredential();
        credentials.setUsername(username);
        credentials.setPassword("badpassword");
        return credentials;
    }

    public abstract ThrottledSubmissionHandlerInterceptor getThrottle();
}
