package org.apereo.cas.web.support;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.IntStream;

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
    @Qualifier("casAuthenticationManager")
    protected AuthenticationManager authenticationManager;

    private static UsernamePasswordCredential credentials(final String username,
                                                          final String password) {
        val credentials = new UsernamePasswordCredential();
        credentials.setUsername(username);
        credentials.assignPassword(password);
        return credentials;
    }

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

    @SneakyThrows
    protected void failLoop(final int trials, final int period, final int expected) {
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

    @SneakyThrows
    protected MockHttpServletResponse login(final String username,
                                            final String password,
                                            final String fromAddress) {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setParameter(CasProtocolConstants.PARAMETER_USERNAME, username);
        request.setParameter(CasProtocolConstants.PARAMETER_PASSWORD, password);
        request.setRemoteAddr(fromAddress);
        request.setRequestURI("/cas/login");
        val context = new MockRequestContext();
        context.setCurrentEvent(new Event(StringUtils.EMPTY, "error"));
        request.setAttribute("flowRequestContext", context);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        getThrottle().preHandle(request, response, getThrottle());

        try {
            val transaction = new DefaultAuthenticationTransactionFactory()
                .newTransaction(CoreAuthenticationTestUtils.getService(),
                    credentials(username, password));
            response.setStatus(HttpServletResponse.SC_OK);
            authenticationManager.authenticate(transaction);
        } catch (final AuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            getThrottle().postHandle(request, response, getThrottle(), null);
        } finally {
            getThrottle().afterCompletion(request, response, getThrottle(), null);
        }
        return response;
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreWebConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasThrottlingConfiguration.class
    })
    static class SharedTestConfiguration {
    }
}
