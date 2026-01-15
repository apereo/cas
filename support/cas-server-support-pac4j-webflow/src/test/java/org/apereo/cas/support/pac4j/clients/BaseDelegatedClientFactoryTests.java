package org.apereo.cas.support.pac4j.clients;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link BaseDelegatedClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseDelegatedClientFactoryTests {
    @Autowired
    @Qualifier(CasSSLContext.BEAN_NAME)
    protected CasSSLContext casSslContext;

    @Autowired
    @Qualifier("pac4jDelegatedClientFactory")
    protected DelegatedIdentityProviderFactory delegatedIdentityProviderFactory;
}

