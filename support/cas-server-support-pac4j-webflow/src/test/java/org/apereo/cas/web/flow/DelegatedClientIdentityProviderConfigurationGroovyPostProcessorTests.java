package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import lombok.val;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationGroovyPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.core.groovy-provider-post-processor.location=classpath:GroovyClientProviderProcessor.groovy")
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
class DelegatedClientIdentityProviderConfigurationGroovyPostProcessorTests {
    @Autowired
    @Qualifier("delegatedClientIdentityProviderConfigurationPostProcessor")
    private DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor;

    @Autowired
    @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
    private DelegatedIdentityProviders identityProviders;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val client = identityProviders.findClient("CasClient", webContext).orElseThrow();
        val provider = DelegatedClientIdentityProviderConfiguration.builder().name(client.getName()).build();
        val clientConfig = Set.of(provider);
        delegatedClientIdentityProviderConfigurationPostProcessor.process(context, clientConfig);
        assertEquals("TestTitle", clientConfig.iterator().next().getTitle());
        delegatedClientIdentityProviderConfigurationPostProcessor.destroy();
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, context.getHttpServletResponse().getStatus());
        assertSame(DelegationAutoRedirectTypes.CLIENT, provider.getAutoRedirectType());
    }
}
