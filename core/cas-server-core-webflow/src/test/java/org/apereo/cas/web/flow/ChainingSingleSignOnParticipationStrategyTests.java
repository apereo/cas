package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ChainingSingleSignOnParticipationStrategyTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyVotesNoInChain() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val chain = new ChainingSingleSignOnParticipationStrategy();
        chain.addStrategy(SingleSignOnParticipationStrategy.alwaysParticipating());
        chain.addStrategy(SingleSignOnParticipationStrategy.neverParticipating());

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertFalse(chain.isParticipating(ssoRequest));
    }

    @Test
    void verifyEmptyChain() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val chain = new ChainingSingleSignOnParticipationStrategy();
        chain.addStrategy(List.of());

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertTrue(chain.isParticipating(ssoRequest));
    }

    @Test
    void verifyVotesYesInChain() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val chain = new ChainingSingleSignOnParticipationStrategy();
        chain.addStrategy(SingleSignOnParticipationStrategy.alwaysParticipating());

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertTrue(chain.isParticipating(ssoRequest));
    }

    @Test
    void verifyVotesNoInChainWithoutSupport() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val chain = new ChainingSingleSignOnParticipationStrategy();
        chain.addStrategy(new SingleSignOnParticipationStrategy() {
            @Override
            public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
                return true;
            }

            @Override
            public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
                return false;
            }
        });
        chain.addStrategy(SingleSignOnParticipationStrategy.neverParticipating());

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertFalse(chain.isParticipating(ssoRequest));
    }

    private static SingleSignOnParticipationRequest getSingleSignOnParticipationRequest(final MockRequestContext context) {
        return SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();
    }
}
