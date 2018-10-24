package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningStandaloneCondition;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link BlackDotIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ConditionalIgnore(condition = RunningStandaloneCondition.class)
@IfProfileValue(name = "blackDotEnabled", value = "true")
public class BlackDotIPAddressIntelligenceServiceTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Test
    public void verifyBannedOperation() {
        val props = new AdaptiveAuthenticationProperties();
        props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
        val service = new BlackDotIPAddressIntelligenceService(props);
        val response = service.examine(new MockRequestContext(), "37.58.59.181");
        assertTrue(response.isBanned());
    }

    @Test
    public void verifyAllowedOperation() {
        val props = new AdaptiveAuthenticationProperties();
        props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
        val service = new BlackDotIPAddressIntelligenceService(props);
        val response = service.examine(new MockRequestContext(), "8.8.8.8");
        assertTrue(response.isAllowed());
    }
}
