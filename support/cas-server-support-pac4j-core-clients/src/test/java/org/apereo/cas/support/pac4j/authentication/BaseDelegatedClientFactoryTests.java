package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jIdentifiableClientProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

/**
 * This is {@link BaseDelegatedClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class
})
public abstract class BaseDelegatedClientFactoryTests {
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasSSLContext.BEAN_NAME)
    protected CasSSLContext casSslContext;

    protected static void configureIdentifiableClient(final Pac4jIdentifiableClientProperties props) {
        props.setId("TestId");
        props.setSecret("TestSecret");
    }

    protected DefaultDelegatedClientFactory getDefaultDelegatedClientFactory(final CasConfigurationProperties casSettings) {
        return new DefaultDelegatedClientFactory(casSettings, List.of(), casSslContext, applicationContext);
    }
}

