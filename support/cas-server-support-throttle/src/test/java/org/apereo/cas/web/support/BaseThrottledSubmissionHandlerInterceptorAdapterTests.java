package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasThrottlingAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Event;
import jakarta.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for submission throttle tests.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public abstract class BaseThrottledSubmissionHandlerInterceptorAdapterTests {
    protected static final String IP_ADDRESS = "192.0.0.1";

    @Autowired
    @Qualifier(AuthenticationManager.BEAN_NAME)
    protected AuthenticationManager authenticationManager;

    private static UsernamePasswordCredential credentials(final String username,
                                                          final String password) {
        val credentials = new UsernamePasswordCredential();
        credentials.setUsername(username);
        credentials.assignPassword(password);
        return credentials;
    }

    @BeforeEach
    void initialize() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr(IP_ADDRESS);
        request.setLocalAddr(IP_ADDRESS);
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyThrottle() throws Throwable {
        /* Ensure that repeated logins BELOW threshold rate are allowed */
        failLoop(3, 1000, HttpStatus.SC_UNAUTHORIZED);

        /* Ensure that repeated logins ABOVE threshold rate are throttled */
        failLoop(3, 200, HttpStatus.SC_LOCKED);

        /* Ensure that slowing down relieves throttle  */
        getThrottle().release();
        Thread.sleep(1000);
        failLoop(3, 1000, HttpStatus.SC_UNAUTHORIZED);
    }

    public abstract ThrottledSubmissionHandlerInterceptor getThrottle();

    protected void failLoop(final int trials, final int period, final int expected) throws Exception {
        /* Seed with something to compare against */

        login("mog", "badpassword", IP_ADDRESS);

        IntStream.range(0, trials).forEach(Unchecked.intConsumer(i -> {
            Thread.sleep(period);
            val status = login("mog", "badpassword", IP_ADDRESS);
            if (i == trials) {
                assertEquals(expected, status.getStatus());
            }
        }));
    }
    protected MockHttpServletResponse login(final String username, final String password,
                                            final String fromAddress) throws Exception {
        val context = MockRequestContext.create();
        val request = context.getHttpServletRequest();
        val response = context.getHttpServletResponse();

        context.setMethod(HttpMethod.POST);
        context.setParameter(CasProtocolConstants.PARAMETER_USERNAME, username);
        context.setParameter(CasProtocolConstants.PARAMETER_PASSWORD, password);
        request.setRemoteAddr(fromAddress);
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        request.setAttribute("flowRequestContext", context);
        request.setRequestURI("/cas/login");
        context.setCurrentEvent(new Event(StringUtils.EMPTY, "error"));
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        getThrottle().preHandle(request, response, getThrottle());

        try {
            val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
                .newTransaction(CoreAuthenticationTestUtils.getService(), credentials(username, password));
            response.setStatus(HttpServletResponse.SC_OK);
            authenticationManager.authenticate(transaction);
        } catch (final Throwable e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            getThrottle().postHandle(request, response, getThrottle(), null);
        } finally {
            getThrottle().afterCompletion(request, response, getThrottle(), null);
        }
        return response;
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasThrottlingAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(CasRegisteredServicesTestConfiguration.class)
    public static class SharedTestConfiguration {
    }
}
