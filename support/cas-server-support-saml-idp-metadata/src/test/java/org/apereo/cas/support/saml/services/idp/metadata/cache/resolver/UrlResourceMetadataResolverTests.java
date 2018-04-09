package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.http.SimpleHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UrlResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CoreSamlConfiguration.class
})
public class UrlResourceMetadataResolverTests {

    @Autowired
    @Qualifier("httpClient")
    private SimpleHttpClient httpClient;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Test
    public void verifyResolverSupports() {
        final SamlIdPProperties props = new SamlIdPProperties();
        final UrlResourceMetadataResolver resolver = new UrlResourceMetadataResolver(props,
            openSamlConfigBean, this.httpClient);
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setMetadataLocation("http://www.testshib.org/metadata/testshib-providers.xml");
        assertTrue(resolver.supports(service));
        service.setMetadataLocation("classpath:sample-service-metadata.xml");
        assertFalse(resolver.supports(service));
    }

    @Test
    public void verifyResolverResolves() {
        final SamlIdPProperties props = new SamlIdPProperties();
        final UrlResourceMetadataResolver resolver = new UrlResourceMetadataResolver(props, openSamlConfigBean, this.httpClient);
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        service.setMetadataLocation("http://www.testshib.org/metadata/testshib-providers.xml");
        final Collection results = resolver.resolve(service);
        assertFalse(results.isEmpty());
    }
}
