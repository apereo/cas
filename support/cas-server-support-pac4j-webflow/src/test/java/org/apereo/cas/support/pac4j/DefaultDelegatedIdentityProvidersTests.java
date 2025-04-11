package org.apereo.cas.support.pac4j;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedIdentityProvidersTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
class DefaultDelegatedIdentityProvidersTests {

    @SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
    abstract static class BaseTests {
        @Autowired
        @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
        protected DelegatedIdentityProviders delegatedIdentityProviders;

        @Autowired
        protected ConfigurableApplicationContext applicationContext;
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.custom.properties.delegation-test.enabled=false",
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
    class MultitenancyTests extends BaseTests {
        @Test
        void verifyOperation() throws Exception {
            val context = MockRequestContext.create(applicationContext)
                .setRequestURI("/tenants/shire/login")
                .setClientInfo();

            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            var allClients = delegatedIdentityProviders.findAllClients(webContext);
            assertEquals(1, allClients.size());

            allClients = delegatedIdentityProviders.findAllClients(webContext);
            assertEquals(1, allClients.size());
        }
    }

    @Nested
    class DefaultTests extends BaseTests {
        @Test
        void verifyOperation() throws Exception {
            val context = MockRequestContext.create(applicationContext);
            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertFalse(delegatedIdentityProviders.findAllClients(webContext).isEmpty());
            assertFalse(delegatedIdentityProviders.findAllClients(RegisteredServiceTestUtils.getService(), webContext).isEmpty());
            assertTrue(delegatedIdentityProviders.findClient("CasClient", webContext).isPresent());
        }
    }
}
