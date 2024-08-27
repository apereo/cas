package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InterruptSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    InterruptWebflowConfigurerTests.SharedTestConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
}, properties = "cas.interrupt.json.location=classpath:/interrupt.json")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class InterruptSingleSignOnParticipationStrategyTests {
    @Autowired
    @Qualifier("interruptSingleSignOnParticipationStrategy")
    private SingleSignOnParticipationStrategy interruptSingleSignOnParticipationStrategy;

    @Test
    void verifyStrategyWithoutInterrupt() throws Throwable {
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(new MockHttpServletRequest())
            .httpServletResponse(new MockHttpServletResponse())
            .requestContext(new MockRequestContext())
            .build();
        assertFalse(interruptSingleSignOnParticipationStrategy.isParticipating(ssoRequest));
    }

    @Test
    void verifyStrategyWithInterruptDisabled() throws Throwable {
        val ctx = new MockRequestContext();
        val response = new InterruptResponse();
        response.setSsoEnabled(false);
        InterruptUtils.putInterruptIn(ctx, response);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(new MockHttpServletRequest())
            .httpServletResponse(new MockHttpServletResponse())
            .requestContext(ctx)
            .build();
        assertTrue(interruptSingleSignOnParticipationStrategy.supports(ssoRequest));
        assertFalse(interruptSingleSignOnParticipationStrategy.isParticipating(ssoRequest));
    }
}
