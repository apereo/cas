package org.jasig.cas.web.support;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.inspektr.common.web.ClientInfo;
import org.jasig.inspektr.common.web.ClientInfoHolder;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import javax.sql.DataSource;

import static org.junit.Assert.*;

/**
 * Unit test for {@link InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/core-context.xml",
        "classpath:/jpaTestApplicationContext.xml",
        "classpath:/inspektrThrottledSubmissionContext.xml"
})
public class InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests extends
                AbstractThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier("inspektrIpAddressUsernameThrottle")
    private InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter throttle;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DataSource dataSource;

    @Override
    @Before
    public void setUp() throws Exception {
        new JdbcTemplate(dataSource).execute("CREATE TABLE COM_AUDIT_TRAIL ( "
                + "AUD_USER      VARCHAR(100)  NOT NULL, "
                + "AUD_CLIENT_IP VARCHAR(15)    NOT NULL, "
                + "AUD_SERVER_IP VARCHAR(15)    NOT NULL, "
                + "AUD_RESOURCE  VARCHAR(100)  NOT NULL, "
                + "AUD_ACTION    VARCHAR(100)  NOT NULL, "
                + "APPLIC_CD     VARCHAR(5)    NOT NULL, "
                + "AUD_DATE      TIMESTAMP      NOT NULL)");
    }

    @Override
    protected AbstractThrottledSubmissionHandlerInterceptorAdapter getThrottle() {
        return throttle;
    }

    @Override
    protected MockHttpServletResponse loginUnsuccessfully(final String username, final String fromAddress)
            throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setParameter("username", username);
        request.setRemoteAddr(fromAddress);
        final MockRequestContext context = new MockRequestContext();
        context.setCurrentEvent(new Event("", "error"));
        request.setAttribute("flowRequestContext", context);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        getThrottle().preHandle(request, response, null);

        try {
            authenticationManager.authenticate(AuthenticationTransaction.wrap(badCredentials(username)));
        } catch (final AuthenticationException e) {
            getThrottle().postHandle(request, response, null, null);
            return response;
        }
        fail("Expected AbstractAuthenticationException");
        return null;
    }

    private UsernamePasswordCredential badCredentials(final String username) {
        final UsernamePasswordCredential credentials = new UsernamePasswordCredential();
        credentials.setUsername(username);
        credentials.setPassword("badpassword");
        return credentials;
    }
}
