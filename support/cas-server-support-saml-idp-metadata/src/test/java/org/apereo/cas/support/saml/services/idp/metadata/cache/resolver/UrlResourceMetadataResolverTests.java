package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.http.HttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

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
    RefreshAutoConfiguration.class
})
public class UrlResourceMetadataResolverTests {

    @Test
    public void verifyResolverSupports() {
        final SamlIdPProperties props = new SamlIdPProperties();
        final UrlResourceMetadataResolver resolver = new UrlResourceMetadataResolver(props,
            mock(OpenSamlConfigBean.class), mock(HttpClient.class));
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setMetadataLocation("http://www.testshib.org/metadata/testshib-providers.xml");
        assertTrue(resolver.supports(service));
        service.setMetadataLocation("classpath:sample-service-metadata.xml");
        assertTrue(resolver.supports(service));
    }
}
