package org.apereo.cas.authentication.principal;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.BaseClient;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedAuthenticationCredentialExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Delegation")
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class DefaultDelegatedAuthenticationCredentialExtractorTests {
    @Autowired
    @Qualifier("delegatedAuthenticationCredentialExtractor")
    private DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
    private DelegatedIdentityProviders identityProviders;
    
    @Test
    void verifyOperation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());
        val client = identityProviders.findClient("FakeClient", webContext).map(BaseClient.class::cast).orElseThrow();
        val results = delegatedAuthenticationCredentialExtractor.extract(client, requestContext);
        assertFalse(results.isEmpty());
        assertNotNull(WebUtils.getCredential(requestContext));
        assertEquals(Ordered.LOWEST_PRECEDENCE, delegatedAuthenticationCredentialExtractor.getOrder());
        assertNotNull(delegatedAuthenticationCredentialExtractor.getName());
    }
}
