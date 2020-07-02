package org.apereo.cas.web.report;

import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredAuthnHandlersTests}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.3.0
 */
@TestPropertySource(properties = "management.endpoint.registeredAuthnHandlers.enabled=true")
@Tag("Simple")
public class RegisteredAuthnHandlersTests extends AbstractCasEndpointTests {

    @Autowired
    @Qualifier("registeredAuthnHandlersReportEndpoint")
    private RegisteredAuthnHandlersEndpoint endpoint;

    @Test
    public void verifyOperation() {
        val handler = new HttpBasedServiceCredentialsAuthenticationHandler(
                StringUtils.EMPTY, null, null, null, new SimpleHttpClientFactoryBean().getObject());
        this.authenticationEventExecutionPlan.registerAuthenticationHandler(handler);

        assertFalse(endpoint.handle().isEmpty());
        assertNotNull(endpoint.fetchAuthnHandler(handler.getName()));
    }
}
