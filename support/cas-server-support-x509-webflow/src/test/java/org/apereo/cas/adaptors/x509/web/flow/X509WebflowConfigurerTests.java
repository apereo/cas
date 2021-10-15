package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.adaptors.x509.config.X509AuthenticationConfiguration;
import org.apereo.cas.adaptors.x509.config.X509CertificateExtractorConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.X509AuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509WebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    X509AuthenticationConfiguration.class,
    X509CertificateExtractorConfiguration.class,
    X509AuthenticationWebflowConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("X509")
public class X509WebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("x509CasMultifactorWebflowCustomizer")
    private CasMultifactorWebflowCustomizer x509CasMultifactorWebflowCustomizer;

    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());

        val flow = (Flow) loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        val state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_X509_START);
        assertNotNull(state);

        assertNotNull(x509CasMultifactorWebflowCustomizer);
        assertTrue(x509CasMultifactorWebflowCustomizer.getCandidateStatesForMultifactorAuthentication()
            .contains(CasWebflowConstants.STATE_ID_X509_START));
    }
}
